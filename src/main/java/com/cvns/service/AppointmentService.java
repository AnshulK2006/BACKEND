package com.cvns.service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.custom_exceptions.ResourceNotFoundException;
import com.cvns.dtos.RequestDtos.AppointmentRequest;
import com.cvns.dtos.RequestDtos.AppointmentStatusRequest;
import com.cvns.dtos.ResponseDtos.AppointmentResponse;
import com.cvns.entities.*;
import com.cvns.entities.AppEnums.*;
import com.cvns.repository.AppointmentRepository;
import com.cvns.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class AppointmentService {
    private static final EnumSet<AppointmentStatus> ACTIVE=EnumSet.of(AppointmentStatus.BOOKED,AppointmentStatus.ACCEPTED);
    private final AppointmentRepository repo;
    private final ChildService children;
    private final ClinicService clinics;
    private final SecurityUtils sec;
    private final NotificationService notifications;
    private final EmailClientService email;

    @Transactional
    public AppointmentResponse book(AppointmentRequest r){
        User u=sec.currentUser();
        if(u.getUserRole()!=UserRole.ROLE_PARENT)throw new ApiException("Only parents can book appointments");
        if(!r.appointmentDate().isAfter(LocalDate.now()))throw new ApiException("Appointment date must be in future");
        Child c=children.entity(r.childId());Clinic clinic=clinics.entity(r.clinicId());
        if(!clinic.isActive()||!clinic.isVerified())throw new ApiException("Clinic is not available for booking");
        if(repo.existsByClinicIdAndAppointmentDateAndAppointmentTimeAndStatusIn(clinic.getId(),r.appointmentDate(),r.appointmentTime(),ACTIVE))throw new ApiException("Selected slot is already booked.");
        if(repo.existsByChildIdAndAppointmentDateAndStatusIn(c.getId(),r.appointmentDate(),ACTIVE))throw new ApiException("Duplicate appointment is not allowed");
        Appointment a=new Appointment();a.setChild(c);a.setClinic(clinic);a.setBookedBy(u);a.setAppointmentDate(r.appointmentDate());a.setAppointmentTime(r.appointmentTime());a.setNotes(r.notes());repo.save(a);
        String m="Appointment booked for "+c.getName()+" at "+clinic.getName()+" on "+r.appointmentDate()+" "+r.appointmentTime();
        notifications.create(u,"Appointment confirmed",m,NotificationType.APPOINTMENT);
        if(clinic.getOwner()!=null)notifications.create(clinic.getOwner(),"New appointment",m,NotificationType.APPOINTMENT);
        email.send(u.getEmail(),"Appointment booked",m,"APPOINTMENT_BOOKED");
        return DtoMapper.appointment(a);
    }

    public List<AppointmentResponse> list(){
        User u=sec.currentUser();
        if(u.getUserRole()==UserRole.ROLE_CLINIC)return repo.findByClinicIdOrderByAppointmentDateDescAppointmentTimeDesc(clinics.current().getId()).stream().map(DtoMapper::appointment).toList();
        if(u.getUserRole()==UserRole.ROLE_PARENT)return repo.findByBookedByIdOrderByAppointmentDateDescAppointmentTimeDesc(u.getId()).stream().map(DtoMapper::appointment).toList();
        return repo.findAll().stream().map(DtoMapper::appointment).toList();
    }

    @Transactional
    public AppointmentResponse status(Long id,AppointmentStatusRequest r){
        Appointment a=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Appointment not found"));User u=sec.currentUser();
        if(u.getUserRole()==UserRole.ROLE_PARENT){
            if(!a.getBookedBy().getId().equals(u.getId())||r.status()!=AppointmentStatus.CANCELLED)throw new ApiException("Parent can only cancel own appointment");
            if(!ACTIVE.contains(a.getStatus()))throw new ApiException("Only active appointment can be cancelled");
        }else if(u.getUserRole()==UserRole.ROLE_CLINIC){
            if(!a.getClinic().getId().equals(clinics.current().getId()))throw new ResourceNotFoundException("Appointment not found");
            boolean valid=(a.getStatus()==AppointmentStatus.BOOKED&&(r.status()==AppointmentStatus.ACCEPTED||r.status()==AppointmentStatus.REJECTED))||(a.getStatus()==AppointmentStatus.ACCEPTED&&r.status()==AppointmentStatus.COMPLETED);
            if(!valid)throw new ApiException("Invalid appointment status transition");
        }else throw new ApiException("Role is not allowed to update appointment");
        a.setStatus(r.status());
        String m="Appointment for "+a.getChild().getName()+" is "+r.status();
        notifications.create(a.getBookedBy(),"Appointment update",m,NotificationType.APPOINTMENT);
        email.send(a.getBookedBy().getEmail(),"Appointment update",m,r.status().name());
        return DtoMapper.appointment(a);
    }
}
