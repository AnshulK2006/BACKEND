package com.cvns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.RequestDtos.ChildRequest;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.ChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService s;
    @GetMapping public ResponseEntity<?> list(){return ResponseEntity.ok(ApiResponse.success("Children",s.list()));}
    @GetMapping("/search") @PreAuthorize("hasAnyRole('ADMIN','CLINIC')") public ResponseEntity<?> search(@RequestParam(defaultValue="")String query){return ResponseEntity.ok(ApiResponse.success("Search result",s.search(query)));}
    @GetMapping("/{id}") public ResponseEntity<?> get(@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success("Child",s.get(id)));}
    @PostMapping @PreAuthorize("hasRole('PARENT')") public ResponseEntity<?> add(@RequestBody @Valid ChildRequest r){return ResponseEntity.status(201).body(ApiResponse.success("Child added",s.add(r)));}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('PARENT','ADMIN')") public ResponseEntity<?> update(@PathVariable Long id,@RequestBody @Valid ChildRequest r){return ResponseEntity.ok(ApiResponse.success("Child updated",s.update(id,r)));}
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('PARENT','ADMIN')") public ResponseEntity<?> delete(@PathVariable Long id){s.delete(id);return ResponseEntity.ok(ApiResponse.success("Child deleted",null));}
}
