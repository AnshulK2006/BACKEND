package com.cvns.entities;
import java.time.*;import com.cvns.entities.AppEnums.AppointmentStatus;import jakarta.persistence.*;import lombok.*;
@NoArgsConstructor @Getter @Setter @ToString(exclude={"child","clinic","bookedBy"}) @Entity @Table(name="appointments") @AttributeOverride(name="id",column=@Column(name="appointment_id"))
public class Appointment extends BaseEntity{
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="child_id") private Child child;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="clinic_id") private Clinic clinic;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booked_by") private User bookedBy;
 @Column(name="appointment_date",nullable=false) private LocalDate appointmentDate; @Column(name="appointment_time",nullable=false) private LocalTime appointmentTime;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private AppointmentStatus status=AppointmentStatus.BOOKED; @Column(length=400) private String notes;
}
