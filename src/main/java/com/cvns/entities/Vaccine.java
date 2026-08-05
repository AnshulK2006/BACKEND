package com.cvns.entities;
import jakarta.persistence.*;import lombok.*;
@NoArgsConstructor @Getter @Setter @Entity @Table(name="vaccines",uniqueConstraints=@UniqueConstraint(columnNames={"name","dose_number"})) @AttributeOverride(name="id",column=@Column(name="vaccine_id"))
public class Vaccine extends BaseEntity{ @Column(nullable=false,length=80) private String name; @Column(length=400) private String description; @Column(name="due_age_months",nullable=false) private Integer dueAgeMonths; @Column(name="dose_number",nullable=false) private Integer doseNumber; }
