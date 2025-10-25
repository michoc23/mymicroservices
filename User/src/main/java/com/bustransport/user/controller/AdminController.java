package com.bustransport.user.controller;

import com.bustransport.user.dto.UserResponse;
import com.bustransport.user.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management", description = "Admin endpoints for user management")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "Get all users (paginated)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.searchUsers(email, role, pageable));
    }

    @PatchMapping("/{userId}/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        adminService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user permanently")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        adminService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }
}