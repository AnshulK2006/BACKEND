package com.cvns.dtos;

import java.time.*;
import com.cvns.entities.AppEnums.*;

public final class ResponseDtos {
    private ResponseDtos() {}

    public record ApiResponse<T>(String status, String message, T data, LocalDateTime timestamp) {
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>("success", message, data, LocalDateTime.now());
        }
        public static ApiResponse<Void> failure(String message) {
            return new ApiResponse<>("failure", message, null, LocalDateTime.now());
        }
    }

    public record AuthResponse(Long userId, String name, String email, UserRole role, String token) {}
    public record RegistrationResponse(String email, boolean otpRequired, int expiresInMinutes) {}
    public record UserResponse(Long id, String firstName, String lastName, String email, String phone,
            LocalDate dob, String address, String city, Double latitude, Double longitude,
            UserRole role, boolean active, boolean emailVerified) {}
    public record ChildResponse(Long id, String name, LocalDate dateOfBirth, Gender gender,
            BloodGroup bloodGroup, String medicalNotes, Long parentId, String parentName) {}
    public record LocationSearchResponse(String osmType, String osmId, String displayName, Double latitude, Double longitude) {}
    public record ClinicResponse(Long id, String name, String email, String phone, String address,
            String city, Double latitude, Double longitude, boolean verified, boolean active) {}
    public record NearbyHospitalResponse(String source, String placeId, Long clinicId, String name,
            String address, Double latitude, Double longitude, Double distanceKm, Double rating,
            Integer userRatingCount, Boolean openNow, String mapUri, String phone) {}
    public record VaccineResponse(Long id, String name, String description, Integer dueAgeMonths, Integer doseNumber) {}
    public record VaccinationResponse(Long recordId, Long vaccineId, String vaccineName, Integer doseNumber,
            LocalDate dueDate, VaccinationStatus status, LocalDate completedDate, String clinicName, String notes) {}
    public record AppointmentResponse(Long id, Long childId, String childName, Long clinicId, String clinicName,
            LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status, String notes) {}
    public record NotificationResponse(Long id, String title, String message, NotificationType type,
            boolean read, LocalDateTime createdAt) {}
    public record DashboardResponse(long children, long upcomingVaccinations, long missedVaccinations,
            long completedVaccinations, long unreadNotifications, long upcomingAppointments) {}
    public record ReportResponse(LocalDate fromDate, LocalDate toDate, long completedVaccinations,
            long pendingVaccinations, long appointments, long vaccinatedChildren) {}
    public record ChatResponse(String answer, boolean vaccinationRelated) {}
}
