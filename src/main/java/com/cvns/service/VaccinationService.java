package com.cvns.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.dtos.RequestDtos.VaccinationUpdateRequest;
import com.cvns.dtos.ResponseDtos.VaccinationResponse;
import com.cvns.entities.*;
import com.cvns.entities.AppEnums.*;
import com.cvns.repository.*;
import com.cvns.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class VaccinationService {
    private final VaccinationRecordRepository records;
    private final VaccineRepository vaccines;
    private final ChildService children;
    private final ClinicService clinics;
    private final SecurityUtils sec;
    private final NotificationService notifications;
    private final EmailClientService email;

    public List<VaccinationResponse> schedule(Long childId){
        Child c=children.entity(childId);
        return vaccines.findAllByOrderByDueAgeMonthsAscDoseNumberAsc().stream().map(v->response(c,v)).toList();
    }

    @Transactional
    public VaccinationResponse update(Long childId,VaccinationUpdateRequest r){
        User current=sec.currentUser();
        if(current.getUserRole()==UserRole.ROLE_PARENT&&r.status()!=VaccinationStatus.COMPLETED)throw new ApiException("Parent can only mark vaccination as completed");
        Vaccine v=vaccines.findById(r.vaccineId()).orElseThrow(()->new ApiException("Vaccine not found"));
        Child c=children.entity(childId);
        VaccinationRecord x=records.findByChildIdAndVaccineId(childId,v.getId()).orElseGet(VaccinationRecord::new);
        if(x.getId()!=null&&x.getStatus()==VaccinationStatus.COMPLETED&&r.status()==VaccinationStatus.COMPLETED)throw new ApiException("Vaccination is already completed");
        if(r.completedDate()!=null&&r.completedDate().isAfter(LocalDate.now()))throw new ApiException("Vaccination completion date cannot be in future");
        x.setChild(c);x.setVaccine(v);x.setStatus(r.status());x.setNotes(r.notes());
        Clinic clinic=null;
        if(current.getUserRole()==UserRole.ROLE_CLINIC)clinic=clinics.current();else if(r.clinicId()!=null)clinic=clinics.entity(r.clinicId());
        x.setClinic(clinic);
        x.setCompletedDate(r.status()==VaccinationStatus.COMPLETED?(r.completedDate()==null?LocalDate.now():r.completedDate()):null);
        records.save(x);
        if(r.status()==VaccinationStatus.COMPLETED){
            String m=v.getName()+" dose "+v.getDoseNumber()+" completed for "+c.getName();
            notifications.create(c.getParent(),"Vaccination completed",m,NotificationType.VACCINATION_COMPLETED);
            email.send(c.getParent().getEmail(),"Vaccination completed",m,"VACCINATION_COMPLETED");
        }
        return response(c,v);
    }

    private VaccinationResponse response(Child c,Vaccine v){
        LocalDate due=c.getDateOfBirth().plusMonths(v.getDueAgeMonths());
        VaccinationRecord r=records.findByChildIdAndVaccineId(c.getId(),v.getId()).orElse(null);
        VaccinationStatus s=r==null?(due.isBefore(LocalDate.now())?VaccinationStatus.MISSED:VaccinationStatus.PENDING):r.getStatus();
        return new VaccinationResponse(r==null?null:r.getId(),v.getId(),v.getName(),v.getDoseNumber(),due,s,r==null?null:r.getCompletedDate(),r==null||r.getClinic()==null?null:r.getClinic().getName(),r==null?null:r.getNotes());
    }
}
