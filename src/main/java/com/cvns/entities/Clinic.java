package com.cvns.entities;
import jakarta.persistence.*;import lombok.*;
@NoArgsConstructor @Getter @Setter @ToString(exclude="owner") @Entity @Table(name="clinics") @AttributeOverride(name="id",column=@Column(name="clinic_id"))
public class Clinic extends BaseEntity{
 @Column(nullable=false,unique=true,length=80) private String name; @Column(nullable=false,unique=true,length=80) private String email;
 @Column(nullable=false,unique=true,length=15) private String phone; @Column(nullable=false,length=160) private String address; @Column(nullable=false,length=50) private String city;
 private Double latitude; private Double longitude; private boolean verified; private boolean active=true;
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_user_id",unique=true) private User owner;
}
