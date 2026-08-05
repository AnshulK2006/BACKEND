package com.cvns.entities;
import java.time.LocalDate;import com.cvns.entities.AppEnums.VaccinationStatus;import jakarta.persistence.*;import lombok.*;
@NoArgsConstructor @Getter @Setter @ToString(exclude={"child","vaccine","clinic"}) @Entity @Table(name="vaccination_records",uniqueConstraints=@UniqueConstraint(columnNames={"child_id","vaccine_id"})) @AttributeOverride(name="id",column=@Column(name="record_id"))
public class VaccinationRecord extends BaseEntity{
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="child_id") private Child child;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="vaccine_id") private Vaccine vaccine;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="clinic_id") private Clinic clinic;
 @Column(name="completed_date") private LocalDate completedDate; @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private VaccinationStatus status=VaccinationStatus.PENDING; @Column(length=500) private String notes;
}
