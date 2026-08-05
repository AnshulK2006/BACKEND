package com.cvns.entities;
import com.cvns.entities.AppEnums.NotificationType;import jakarta.persistence.*;import lombok.*;
@NoArgsConstructor @Getter @Setter @ToString(exclude="recipient") @Entity @Table(name="notifications") @AttributeOverride(name="id",column=@Column(name="notification_id"))
public class Notification extends BaseEntity{
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="recipient_id") private User recipient;
 @Column(nullable=false,length=100) private String title; @Column(nullable=false,length=600) private String message;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private NotificationType type; @Column(name="is_read") private boolean read;
}
