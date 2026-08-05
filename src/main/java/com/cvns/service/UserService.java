package com.cvns.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.custom_exceptions.ResourceNotFoundException;
import com.cvns.dtos.RequestDtos.AdminUserRequest;
import com.cvns.dtos.RequestDtos.ProfileRequest;
import com.cvns.dtos.ResponseDtos.UserResponse;
import com.cvns.entities.User;
import com.cvns.entities.AppEnums.UserRole;
import com.cvns.repository.*;
import com.cvns.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final ChildRepository children;
    private final VaccinationRecordRepository records;
    private final AppointmentRepository appointments;
    private final NotificationRepository notifications;
    private final EmailVerificationOtpRepository otps;
    private final SecurityUtils sec;

    public UserResponse profile() {
        return DtoMapper.user(sec.currentUser());
    }

    @Transactional
    public UserResponse update(ProfileRequest r) {
        User u = sec.currentUser();
        if (!u.getPhone().equals(r.phone()) && repo.existsByPhone(r.phone()))
            throw new ApiException("Phone is already registered");
        if (u.getUserRole() == UserRole.ROLE_PARENT && (r.latitude() == null || r.longitude() == null))
            throw new ApiException("Please select your location on the map");
        set(u, r.firstName(), r.lastName(), r.phone(), r.dob(), r.address(), r.city(), r.latitude(), r.longitude());
        return DtoMapper.user(u);
    }

    public List<UserResponse> parents(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return repo.findByUserRoleOrderByFirstNameAsc(UserRole.ROLE_PARENT).stream()
                .filter(u -> q.isBlank() || (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(q)
                        || u.getEmail().toLowerCase().contains(q) || u.getPhone().contains(q))
                .map(DtoMapper::user).toList();
    }

    @Transactional
    public UserResponse updateParent(Long id, AdminUserRequest r) {
        User u = parent(id);
        if (!u.getPhone().equals(r.phone()) && repo.existsByPhone(r.phone()))
            throw new ApiException("Phone is already registered");
        set(u, r.firstName(), r.lastName(), r.phone(), r.dob(), r.address(), r.city(), r.latitude(), r.longitude());
        if (r.active() != null) u.setActive(r.active());
        return DtoMapper.user(u);
    }

    @Transactional
    public UserResponse active(Long id, boolean value) {
        User u = parent(id);
        u.setActive(value);
        return DtoMapper.user(u);
    }

    @Transactional
    public void deleteParent(Long id) {
        User u = parent(id);
        appointments.deleteByBookedById(id);
        appointments.deleteByChildParentId(id);
        records.deleteByChildParentId(id);
        children.deleteByParentId(id);
        notifications.deleteByRecipientId(id);
        otps.deleteByUserId(id);
        repo.delete(u);
    }

    private void set(User u, String firstName, String lastName, String phone, java.time.LocalDate dob,
            String address, String city, Double latitude, Double longitude) {
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setPhone(phone);
        u.setDob(dob);
        u.setAddress(address);
        u.setCity(city);
        u.setLatitude(latitude);
        u.setLongitude(longitude);
    }

    private User parent(Long id) {
        User u = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        if (u.getUserRole() != UserRole.ROLE_PARENT) throw new ResourceNotFoundException("Parent not found");
        return u;
    }
}
