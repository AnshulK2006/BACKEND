package com.cvns.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.cvns.custom_exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailClientService {
    private final RestClient client;

    public EmailClientService(RestClient.Builder builder,
            @Value("${services.email.base-url}") String baseUrl) {
        client = builder.baseUrl(baseUrl).build();
    }

    public void send(String to, String subject, String message, String type) {
        try {
            deliver(Map.of("to", to, "subject", subject, "message", message, "type", type));
        } catch (Exception e) {
            log.warn("Email service unavailable: {}", e.getMessage());
        }
    }

    public void sendVerificationOtp(String to, String name, String otp, int expiryMinutes) {
        try {
            deliver(Map.of(
                    "to", to,
                    "subject", "Your VaccineCare verification code",
                    "message", "Hello " + name + ", use this code to verify your email. It expires in "
                            + expiryMinutes + " minutes. Do not share this code with anyone.",
                    "type", "EMAIL_OTP",
                    "otpCode", otp));
        } catch (Exception e) {
            log.error("Unable to send verification OTP to {}: {}", to, e.getMessage());
            throw new ApiException("Unable to send verification email. Check SMTP configuration and try again.");
        }
    }

    private void deliver(Map<String, String> body) {
        client.post().uri("/api/email/send").body(body).retrieve().toBodilessEntity();
    }
}
