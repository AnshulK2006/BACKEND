package com.cvns.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.RequestDtos.*;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration saved. Verify the OTP sent to your email.",
                        service.register(request)));
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<?> verifyEmailOtp(@RequestBody @Valid VerifyEmailOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully",
                service.verifyEmailOtp(request)));
    }

    @PostMapping("/resend-email-otp")
    public ResponseEntity<?> resendEmailOtp(@RequestBody @Valid ResendEmailOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("A new OTP was sent to your email",
                service.resendEmailOtp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", service.login(request)));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> password(@RequestBody @Valid ChangePasswordRequest request) {
        service.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed", null));
    }
}
