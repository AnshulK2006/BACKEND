package com.cvns.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cvns.entities.Appointment;
import com.cvns.entities.AppEnums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    boolean existsByClinicIdAndAppointmentDateAndAppointmentTimeAndStatusIn(Long clinicId,LocalDate date,LocalTime time,Collection<AppointmentStatus> status);
    boolean existsByChildIdAndAppointmentDateAndStatusIn(Long childId,LocalDate date,Collection<AppointmentStatus> status);
    List<Appointment> findByBookedByIdOrderByAppointmentDateDescAppointmentTimeDesc(Long id);
    List<Appointment> findByClinicIdOrderByAppointmentDateDescAppointmentTimeDesc(Long id);
    long countByBookedByIdAndAppointmentDateGreaterThanEqualAndStatusIn(Long id,LocalDate date,Collection<AppointmentStatus> status);
    long countByClinicIdAndAppointmentDateBetween(Long id,LocalDate from,LocalDate to);
    List<Appointment> findByAppointmentDateAndStatusIn(LocalDate date,Collection<AppointmentStatus> status);
    void deleteByBookedById(Long userId);
    void deleteByChildId(Long childId);
    void deleteByChildParentId(Long parentId);
    void deleteByClinicId(Long clinicId);
}
