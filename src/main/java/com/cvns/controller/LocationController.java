package com.cvns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.LocationSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationSearchService service;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success("Location results", service.search(query)));
    }
}
