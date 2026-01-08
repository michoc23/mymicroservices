package com.bustransport.user.controller;

import com.bustransport.user.dto.ChangePasswordRequest;
import com.bustransport.user.dto.TicketConsumptionResponse;
import com.bustransport.user.dto.UpdateProfileRequest;
import com.bustransport.user.dto.UserResponse;
import com.bustransport.user.repository.UserRepository;
import com.bustransport.user.security.JwtUtil;
import com.bustransport.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "User Management", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication,
            HttpServletRequest request) {
        Long userId = extractUserIdFromAuth(authentication, request);
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
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user account")
    public ResponseEntity<Void> deleteAccount(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<Void> activateAccount(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        userService.activateAccount(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<Void> deactivateAccount(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        userService.deactivateAccount(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/tickets/{ticketId}/consumption")
    @Operation(summary = "Check if a ticket is consumed or not", 
               description = "Check the consumption status of a ticket by ticket ID. Verifies ticket ownership and returns consumption status.")
    public ResponseEntity<TicketConsumptionResponse> checkTicketConsumption(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @PathVariable Long ticketId) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        TicketConsumptionResponse response = userService.checkTicketConsumption(userId, ticketId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/tickets/qr/{qrCode}/consumption")
    @Operation(summary = "Check if a ticket is consumed by QR code", 
               description = "Check the consumption status of a ticket by QR code. Verifies ticket ownership and returns consumption status.")
    public ResponseEntity<TicketConsumptionResponse> checkTicketConsumptionByQrCode(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @PathVariable String qrCode) {
        Long userId = extractUserIdFromAuth(authentication, httpRequest);
        TicketConsumptionResponse response = userService.checkTicketConsumptionByQrCode(userId, qrCode);
        return ResponseEntity.ok(response);
    }

    private Long extractUserIdFromAuth(Authentication authentication, HttpServletRequest request) {
        try {
            // Try to extract from JWT token first
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long userId = jwtUtil.extractUserId(token);
                if (userId != null) {
                    return userId;
                }
            }
            
            // Fallback: extract from authentication principal (email) and look up user
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String email = userDetails.getUsername();
                return userRepository.findByEmail(email)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            }
            
            // Last resort: return null or throw exception
            throw new RuntimeException("Unable to extract user ID from authentication");
        } catch (Exception e) {
            throw new RuntimeException("Authentication error: " + e.getMessage(), e);
        }
    }

    // Overloaded method for backward compatibility
    private Long extractUserIdFromAuth(Authentication authentication) {
        // This method is deprecated - use the one with HttpServletRequest
        // For now, try to get from email
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(1L); // Fallback
        }
        return 1L; // Placeholder fallback
    }
}