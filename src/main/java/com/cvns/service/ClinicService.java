package com.cvns.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.custom_exceptions.ResourceNotFoundException;
import com.cvns.dtos.RequestDtos.ClinicRequest;
import com.cvns.dtos.ResponseDtos.ClinicResponse;
import com.cvns.dtos.ResponseDtos.NearbyHospitalResponse;
import com.cvns.entities.Clinic;
import com.cvns.entities.User;
import com.cvns.entities.AppEnums.UserRole;
import com.cvns.repository.*;
import com.cvns.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClinicService {
    private final ClinicRepository repo;
    private final AppointmentRepository appointments;
    private final VaccinationRecordRepository records;
    private final NotificationRepository notifications;
    private final EmailVerificationOtpRepository otps;
    private final UserRepository users;
    private final SecurityUtils sec;

    public List<ClinicResponse> publicList(String q) {
        if (q == null || q.isBlank())
            return repo.findByActiveTrueAndVerifiedTrueOrderByNameAsc().stream().map(DtoMapper::clinic).toList();
        return repo.findByNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrderByNameAsc(q, q).stream()
                .filter(c -> c.isActive() && c.isVerified()).map(DtoMapper::clinic).toList();
    }

    public List<NearbyHospitalResponse> nearby(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null) throw new ApiException("Parent location is required");
        double radius = radiusKm == null ? 10.0 : Math.min(Math.max(radiusKm, 1.0), 50.0);
        List<NearbyHospitalResponse> result = new ArrayList<>();

        repo.findByActiveTrueAndVerifiedTrueOrderByNameAsc().stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(c -> registered(c, latitude, longitude))
                .filter(c -> c.distanceKm() <= radius)
                .forEach(result::add);

        return result.stream()
                .sorted(Comparator.comparing(NearbyHospitalResponse::distanceKm,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(25)
                .toList();
    }

    public List<ClinicResponse> all(String query, Boolean verified) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return repo.findAll().stream()
                .filter(c -> q.isBlank() || c.getName().toLowerCase().contains(q)
                        || c.getCity().toLowerCase().contains(q))
                .filter(c -> verified == null || c.isVerified() == verified)
                .map(DtoMapper::clinic).toList();
    }

    public ClinicResponse profile() {
        return DtoMapper.clinic(current());
    }

    @Transactional
    public ClinicResponse add(ClinicRequest r) {
        Clinic c = new Clinic();
        set(c, r);
        c.setVerified(false);
        return DtoMapper.clinic(repo.save(c));
    }

    @Transactional
    public ClinicResponse updateOwn(ClinicRequest r) {
        if (sec.currentUser().getUserRole() != UserRole.ROLE_CLINIC) throw new ApiException("Clinic role required");
        Clinic c = current();
        set(c, r);
        User owner = c.getOwner();
        if (owner != null) {
            owner.setAddress(r.address());
            owner.setCity(r.city());
            owner.setLatitude(r.latitude());
            owner.setLongitude(r.longitude());
        }
        return DtoMapper.clinic(c);
    }

    @Transactional
    public ClinicResponse update(Long id, ClinicRequest r) {
        Clinic c = entity(id);
        set(c, r);
        return DtoMapper.clinic(c);
    }

    @Transactional
    public ClinicResponse verify(Long id, boolean value) {
        Clinic c = entity(id);
        c.setVerified(value);
        return DtoMapper.clinic(c);
    }

    @Transactional
    public ClinicResponse active(Long id, boolean value) {
        Clinic c = entity(id);
        c.setActive(value);
        return DtoMapper.clinic(c);
    }

    @Transactional
    public void delete(Long id) {
        Clinic c = entity(id);
        User owner = c.getOwner();
        appointments.deleteByClinicId(id);
        records.clearClinic(id);
        repo.delete(c);
        repo.flush();
        if (owner != null) {
            notifications.deleteByRecipientId(owner.getId());
            otps.deleteByUserId(owner.getId());
            users.delete(owner);
        }
    }

    public Clinic entity(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Clinic not found"));
    }

    public Clinic current() {
        return repo.findByOwnerId(sec.currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinic profile not found"));
    }

    private void set(Clinic c, ClinicRequest r) {
        c.setName(r.name());
        c.setEmail(r.email());
        c.setPhone(r.phone());
        c.setAddress(r.address());
        c.setCity(r.city());
        c.setLatitude(r.latitude());
        c.setLongitude(r.longitude());
        if (r.active() != null) c.setActive(r.active());
    }

    private NearbyHospitalResponse registered(Clinic c, double latitude, double longitude) {
        double distance = Math.round(OpenStreetMapService.distanceKm(latitude, longitude,
                c.getLatitude(), c.getLongitude()) * 100.0) / 100.0;
        String mapUri = "https://www.openstreetmap.org/?mlat=" + c.getLatitude() + "&mlon=" + c.getLongitude()
                + "#map=17/" + c.getLatitude() + "/" + c.getLongitude();
        return new NearbyHospitalResponse("REGISTERED", null, c.getId(), c.getName(), c.getAddress() + ", " + c.getCity(),
                c.getLatitude(), c.getLongitude(), distance, null, null, null, mapUri, c.getPhone());
    }
}
