package com.cvns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cvns.dtos.RequestDtos.AdminUserRequest;
import com.cvns.dtos.RequestDtos.ProfileRequest;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import com.cvns.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService s;
    @GetMapping("/profile") public ResponseEntity<?> profile(){return ResponseEntity.ok(ApiResponse.success("Profile",s.profile()));}
    @PutMapping("/profile") public ResponseEntity<?> update(@RequestBody @Valid ProfileRequest r){return ResponseEntity.ok(ApiResponse.success("Profile updated",s.update(r)));}
    @GetMapping("/parents") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> parents(@RequestParam(required=false)String query){return ResponseEntity.ok(ApiResponse.success("Parents",s.parents(query)));}
    @PutMapping("/parents/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> updateParent(@PathVariable Long id,@RequestBody @Valid AdminUserRequest r){return ResponseEntity.ok(ApiResponse.success("Parent updated",s.updateParent(id,r)));}
    @PatchMapping("/{id}/active") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> active(@PathVariable Long id,@RequestParam boolean value){return ResponseEntity.ok(ApiResponse.success("Account updated",s.active(id,value)));}
    @DeleteMapping("/parents/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> delete(@PathVariable Long id){s.deleteParent(id);return ResponseEntity.ok(ApiResponse.success("Parent deleted",null));}
}
