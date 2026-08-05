package com.cvns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.RequestDtos.VaccinationUpdateRequest;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.VaccinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vaccinations")
@RequiredArgsConstructor
public class VaccinationController {
    private final VaccinationService s;
    @GetMapping("/child/{id}") @PreAuthorize("hasAnyRole('PARENT','CLINIC','ADMIN')") public ResponseEntity<?> schedule(@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success("Vaccination schedule",s.schedule(id)));}
    @PutMapping("/child/{id}") @PreAuthorize("hasAnyRole('PARENT','CLINIC')") public ResponseEntity<?> update(@PathVariable Long id,@RequestBody @Valid VaccinationUpdateRequest r){return ResponseEntity.ok(ApiResponse.success("Vaccination updated",s.update(id,r)));}
}
