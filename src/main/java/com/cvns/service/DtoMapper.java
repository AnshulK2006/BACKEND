package com.cvns.service;

import com.cvns.dtos.ResponseDtos.*;
import com.cvns.entities.*;

public final class DtoMapper {
    private DtoMapper() {}

    public static UserResponse user(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(),
                u.getDob(), u.getAddress(), u.getCity(), u.getLatitude(), u.getLongitude(),
                u.getUserRole(), u.isActive(), u.isEmailVerified());
    }

    public static ChildResponse child(Child c) {
        return new ChildResponse(c.getId(), c.getName(), c.getDateOfBirth(), c.getGender(), c.getBloodGroup(),
                c.getMedicalNotes(), c.getParent().getId(),
                c.getParent().getFirstName() + " " + c.getParent().getLastName());
    }

    public static ClinicResponse clinic(Clinic c) {
        return new ClinicResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress(), c.getCity(),
                c.getLatitude(), c.getLongitude(), c.isVerified(), c.isActive());
    }

    public static VaccineResponse vaccine(Vaccine v) {
        return new VaccineResponse(v.getId(), v.getName(), v.getDescription(), v.getDueAgeMonths(), v.getDoseNumber());
    }

    public static AppointmentResponse appointment(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getChild().getId(), a.getChild().getName(),
                a.getClinic().getId(), a.getClinic().getName(), a.getAppointmentDate(),
                a.getAppointmentTime(), a.getStatus(), a.getNotes());
    }

    public static NotificationResponse notification(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead(), n.getCreatedAt());
    }
}
