package com.cvns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.RequestDtos.AppointmentRequest;
import com.cvns.dtos.RequestDtos.AppointmentStatusRequest;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService s;
    @GetMapping @PreAuthorize("hasAnyRole('PARENT','CLINIC','ADMIN')") public ResponseEntity<?> list(){return ResponseEntity.ok(ApiResponse.success("Appointments",s.list()));}
    @PostMapping @PreAuthorize("hasRole('PARENT')") public ResponseEntity<?> book(@RequestBody @Valid AppointmentRequest r){return ResponseEntity.status(201).body(ApiResponse.success("Appointment booked",s.book(r)));}
    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('PARENT','CLINIC')") public ResponseEntity<?> status(@PathVariable Long id,@RequestBody @Valid AppointmentStatusRequest r){return ResponseEntity.ok(ApiResponse.success("Appointment updated",s.status(id,r)));}
}
