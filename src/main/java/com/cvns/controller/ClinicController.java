package com.cvns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.RequestDtos.ClinicRequest;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinics")
@RequiredArgsConstructor
public class ClinicController {
    private final ClinicService service;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success("Clinics", service.publicList(query)));
    }

    @GetMapping("/nearby")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> nearby(@RequestParam Double latitude, @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm) {
        return ResponseEntity.ok(ApiResponse.success("Nearby clinics",
                service.nearby(latitude, longitude, radiusKm)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> all(@RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean verified) {
        return ResponseEntity.ok(ApiResponse.success("All clinics", service.all(query, verified)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> add(@RequestBody @Valid ClinicRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Clinic added", service.add(request)));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLINIC')")
    public ResponseEntity<?> profile() {
        return ResponseEntity.ok(ApiResponse.success("Clinic profile", service.profile()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CLINIC')")
    public ResponseEntity<?> own(@RequestBody @Valid ClinicRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Clinic updated", service.updateOwn(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid ClinicRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Clinic updated", service.update(id, request)));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verify(@PathVariable Long id, @RequestParam boolean value) {
        return ResponseEntity.ok(ApiResponse.success("Verification updated", service.verify(id, value)));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> active(@PathVariable Long id, @RequestParam boolean value) {
        return ResponseEntity.ok(ApiResponse.success("Clinic status updated", service.active(id, value)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Clinic deleted", null));
    }
}
