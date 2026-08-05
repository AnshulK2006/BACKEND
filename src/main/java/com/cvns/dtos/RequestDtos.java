package com.cvns.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import com.cvns.entities.AppEnums.*;
import jakarta.validation.constraints.*;

public final class RequestDtos {
    private RequestDtos() {}

    private static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

    public record RegisterRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @Email @NotBlank String email,
            @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$") String phone,
            @NotBlank @Size(min = 8) @Pattern(regexp = STRONG_PASSWORD) String password,
            @PastOrPresent LocalDate dob,
            String address,
            String city,
            @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @NotNull UserRole role,
            String clinicName) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record VerifyEmailOtpRequest(
            @Email @NotBlank String email,
            @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain exactly 6 digits") String otp) {}

    public record ResendEmailOtpRequest(@Email @NotBlank String email) {}

    public record ChangePasswordRequest(
            @NotBlank String oldPassword,
            @NotBlank @Size(min = 8) @Pattern(regexp = STRONG_PASSWORD) String newPassword) {}

    public record ProfileRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$") String phone,
            @PastOrPresent LocalDate dob,
            String address,
            String city,
            @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {}

    public record AdminUserRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$") String phone,
            @PastOrPresent LocalDate dob,
            String address,
            String city,
            @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            Boolean active) {}

    public record ChildRequest(
            @NotBlank String name,
            @NotNull @PastOrPresent LocalDate dateOfBirth,
            @NotNull Gender gender,
            BloodGroup bloodGroup,
            String medicalNotes) {}

    public record ClinicRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank @Pattern(regexp = "^[0-9]{10,15}$") String phone,
            @NotBlank String address,
            @NotBlank String city,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            Boolean active) {}

    public record VaccineRequest(
            @NotBlank String name,
            String description,
            @NotNull @PositiveOrZero Integer dueAgeMonths,
            @NotNull @Positive Integer doseNumber) {}

    public record VaccinationUpdateRequest(
            @NotNull Long vaccineId,
            Long clinicId,
            @NotNull VaccinationStatus status,
            @PastOrPresent LocalDate completedDate,
            String notes) {}

    public record AppointmentRequest(
            @NotNull Long childId,
            @NotNull Long clinicId,
            @NotNull @Future LocalDate appointmentDate,
            @NotNull LocalTime appointmentTime,
            String notes) {}

    public record AppointmentStatusRequest(@NotNull AppointmentStatus status) {}
    public record ChatRequest(@NotBlank String message) {}
}
