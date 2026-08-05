package com.cvns.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cvns.entities.EmailVerificationOtp;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {
    Optional<EmailVerificationOtp> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
