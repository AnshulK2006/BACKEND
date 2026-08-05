package com.cvns.entities;
import java.time.LocalDate;import com.cvns.entities.AppEnums.*;import jakarta.persistence.*;import lombok.*;
@NoArgsConstructor @Getter @Setter @ToString(exclude="parent") @Entity @Table(name="children") @AttributeOverride(name="id",column=@Column(name="child_id"))
public class Child extends BaseEntity{
 @Column(nullable=false,length=50) private String name; @Column(name="date_of_birth",nullable=false) private LocalDate dateOfBirth;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private Gender gender;
 @Enumerated(EnumType.STRING) @Column(name="blood_group",length=20) private BloodGroup bloodGroup;
 @Column(name="medical_notes",length=500) private String medicalNotes;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="parent_id",nullable=false) private User parent;
}
