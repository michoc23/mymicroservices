package com.bustransport.user.controller;

import com.bustransport.user.dto.ChangePasswordRequest;
import com.bustransport.user.dto.UpdateProfileRequest;
import com.bustransport.user.dto.UserResponse;
import com.bustransport.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "User Management", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = extractUserIdFromAuth(authentication);
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user account")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<Void> activateAccount(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        userService.activateAccount(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<Void> deactivateAccount(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        userService.deactivateAccount(userId);
        return ResponseEntity.ok().build();
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        // In production, extract from JWT claims
        return 1L; // Placeholder
    }
}