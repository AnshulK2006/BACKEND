package com.cvns.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.custom_exceptions.ResourceNotFoundException;
import com.cvns.dtos.RequestDtos.ChildRequest;
import com.cvns.dtos.ResponseDtos.ChildResponse;
import com.cvns.entities.Child;
import com.cvns.entities.User;
import com.cvns.entities.AppEnums.UserRole;
import com.cvns.repository.*;
import com.cvns.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class ChildService {
    private final ChildRepository repo;
    private final VaccinationRecordRepository records;
    private final AppointmentRepository appointments;
    private final SecurityUtils sec;

    public List<ChildResponse> list(){
        User u=sec.currentUser();
        List<Child>x=u.getUserRole()==UserRole.ROLE_PARENT?repo.findByParentIdOrderByNameAsc(u.getId()):repo.findAll();
        return x.stream().map(DtoMapper::child).toList();
    }

    public ChildResponse get(Long id){return DtoMapper.child(entity(id));}

    @Transactional
    public ChildResponse add(ChildRequest r){
        User u=sec.currentUser();if(u.getUserRole()!=UserRole.ROLE_PARENT)throw new ApiException("Only parents can add children");
        Child c=new Child();set(c,r);c.setParent(u);return DtoMapper.child(repo.save(c));
    }

    @Transactional
    public ChildResponse update(Long id,ChildRequest r){Child c=entity(id);set(c,r);return DtoMapper.child(c);}

    @Transactional
    public void delete(Long id){Child c=entity(id);appointments.deleteByChildId(id);records.deleteByChildId(id);repo.delete(c);}

    public List<ChildResponse> search(String q){
        User u=sec.currentUser();if(u.getUserRole()==UserRole.ROLE_PARENT)return list();
        String x=q==null?"":q.trim();
        if(x.matches("[0-9]+"))return repo.findById(Long.valueOf(x)).stream().map(DtoMapper::child).toList();
        return repo.findByNameContainingIgnoreCaseOrParentFirstNameContainingIgnoreCaseOrParentLastNameContainingIgnoreCase(x,x,x).stream().map(DtoMapper::child).toList();
    }

    public Child entity(Long id){
        Child c=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Child not found"));
        User u=sec.currentUser();
        if(u.getUserRole()==UserRole.ROLE_PARENT&&!c.getParent().getId().equals(u.getId()))throw new ResourceNotFoundException("Child not found");
        return c;
    }

    private void set(Child c,ChildRequest r){
        if(r.dateOfBirth().isAfter(LocalDate.now()))throw new ApiException("Date of birth cannot be in future");
        c.setName(r.name());c.setDateOfBirth(r.dateOfBirth());c.setGender(r.gender());c.setBloodGroup(r.bloodGroup());c.setMedicalNotes(r.medicalNotes());
    }
}
