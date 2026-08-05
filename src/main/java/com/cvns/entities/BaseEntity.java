package com.cvns.entities;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import lombok.Getter;import lombok.Setter;import lombok.ToString;
@MappedSuperclass @Getter @Setter @ToString
public class BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
 @UpdateTimestamp @Column(name="updated_at") private LocalDateTime updatedAt;
}
