package com.cvns.entities;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "email_verification_otps")
@AttributeOverride(name = "id", column = @Column(name = "otp_id"))
public class EmailVerificationOtp extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;
}
