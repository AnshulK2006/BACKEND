package com.cvns.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.dtos.RequestDtos.*;
import com.cvns.dtos.ResponseDtos.AuthResponse;
import com.cvns.dtos.ResponseDtos.RegistrationResponse;
import com.cvns.entities.*;
import com.cvns.entities.AppEnums.UserRole;
import com.cvns.repository.*;
import com.cvns.security.*;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository users;
    private final ClinicRepository clinics;
    private final EmailVerificationOtpRepository otps;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    private final JwtUtils jwt;
    private final SecurityUtils sec;
    private final EmailClientService email;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public RegistrationResponse register(RegisterRequest r) {
        String mail = normalize(r.email());
        if (r.role() == UserRole.ROLE_ADMIN && users.countByUserRole(UserRole.ROLE_ADMIN) > 0)
            throw new ApiException("Only one administrator is allowed");
        if (users.existsByEmail(mail)) throw new ApiException("Email is already registered");
        if (users.existsByPhone(r.phone())) throw new ApiException("Phone is already registered");
        if (r.role() == UserRole.ROLE_CLINIC && (r.clinicName() == null || r.clinicName().isBlank()))
            throw new ApiException("Clinic name is required");
        if ((r.role() == UserRole.ROLE_PARENT || r.role() == UserRole.ROLE_CLINIC)
                && (r.latitude() == null || r.longitude() == null))
            throw new ApiException("Please select your location on the map");

        User u = new User();
        u.setFirstName(r.firstName());
        u.setLastName(r.lastName());
        u.setEmail(mail);
        u.setPhone(r.phone());
        u.setPassword(encoder.encode(r.password()));
        u.setDob(r.dob());
        u.setAddress(r.address());
        u.setCity(r.city());
        u.setLatitude(r.latitude());
        u.setLongitude(r.longitude());
        u.setUserRole(r.role());
        u.setEmailVerified(false);
        users.save(u);

        if (r.role() == UserRole.ROLE_CLINIC) {
            Clinic c = new Clinic();
            c.setName(r.clinicName());
            c.setEmail(u.getEmail());
            c.setPhone(u.getPhone());
            c.setAddress(r.address() == null ? "Update clinic address" : r.address());
            c.setCity(r.city() == null ? "Update city" : r.city());
            c.setLatitude(r.latitude());
            c.setLongitude(r.longitude());
            c.setActive(false);
            c.setOwner(u);
            clinics.save(c);
        }

        issueOtp(u, false);
        return registrationResponse(u);
    }

    public AuthResponse login(LoginRequest r) {
        String mail = normalize(r.email());
        manager.authenticate(new UsernamePasswordAuthenticationToken(mail, r.password()));
        User u = users.findByEmail(mail).orElseThrow(() -> new ApiException("Invalid login"));
        if (!u.isActive()) throw new ApiException("Account is inactive");
        if (!u.isEmailVerified())
            throw new ApiException("Email is not verified. Enter the OTP sent to your email or request a new OTP.");
        return response(u);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse verifyEmailOtp(VerifyEmailOtpRequest r) {
        User u = users.findByEmail(normalize(r.email()))
                .orElseThrow(() -> new ApiException("No account found for this email"));
        if (u.isEmailVerified()) return response(u);

        EmailVerificationOtp otp = otps.findByUserId(u.getId())
                .orElseThrow(() -> new ApiException("No active OTP found. Request a new OTP."));
        LocalDateTime now = LocalDateTime.now();
        if (otp.getExpiresAt().isBefore(now))
            throw new ApiException("OTP has expired. Request a new OTP.");
        if (otp.getFailedAttempts() >= MAX_OTP_ATTEMPTS)
            throw new ApiException("Too many incorrect attempts. Request a new OTP.");

        if (!encoder.matches(r.otp(), otp.getCodeHash())) {
            otp.setFailedAttempts(otp.getFailedAttempts() + 1);
            int remaining = MAX_OTP_ATTEMPTS - otp.getFailedAttempts();
            if (remaining <= 0) throw new ApiException("Incorrect OTP. Request a new OTP to continue.");
            throw new ApiException("Incorrect OTP. " + remaining + " attempt(s) remaining.");
        }

        u.setEmailVerified(true);
        clinics.findByOwnerId(u.getId()).ifPresent(c -> c.setActive(true));
        otps.delete(otp);
        email.send(u.getEmail(), "Welcome to VaccineCare",
                "Hello " + u.getFirstName() + ", your email is verified and registration is complete.",
                "REGISTRATION");
        return response(u);
    }

    @Transactional
    public RegistrationResponse resendEmailOtp(ResendEmailOtpRequest r) {
        User u = users.findByEmail(normalize(r.email()))
                .orElseThrow(() -> new ApiException("No account found for this email"));
        if (u.isEmailVerified()) throw new ApiException("Email is already verified. Please login.");
        issueOtp(u, true);
        return registrationResponse(u);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest r) {
        User u = sec.currentUser();
        if (!encoder.matches(r.oldPassword(), u.getPassword())) throw new ApiException("Old password is incorrect");
        u.setPassword(encoder.encode(r.newPassword()));
    }

    private void issueOtp(User u, boolean enforceCooldown) {
        LocalDateTime now = LocalDateTime.now();
        EmailVerificationOtp otp = otps.findByUserId(u.getId()).orElseGet(EmailVerificationOtp::new);
        if (enforceCooldown && otp.getLastSentAt() != null) {
            long elapsed = Duration.between(otp.getLastSentAt(), now).getSeconds();
            if (elapsed < OTP_RESEND_COOLDOWN_SECONDS)
                throw new ApiException("Please wait " + (OTP_RESEND_COOLDOWN_SECONDS - elapsed)
                        + " seconds before requesting another OTP.");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        otp.setUser(u);
        otp.setCodeHash(encoder.encode(code));
        otp.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        otp.setLastSentAt(now);
        otp.setFailedAttempts(0);
        otps.save(otp);
        email.sendVerificationOtp(u.getEmail(), u.getFirstName(), code, OTP_EXPIRY_MINUTES);
    }

    private RegistrationResponse registrationResponse(User u) {
        return new RegistrationResponse(u.getEmail(), true, OTP_EXPIRY_MINUTES);
    }

    private AuthResponse response(User u) {
        return new AuthResponse(u.getId(), u.getFirstName() + " " + u.getLastName(), u.getEmail(),
                u.getUserRole(), jwt.generateJwt(new CustomUserDetails(u)));
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
