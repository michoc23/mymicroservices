package com.bustransport.user.controller;

import com.bustransport.user.dto.PassengerResponse;
import com.bustransport.user.service.PassengerService;
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
@RequestMapping("/passengers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('PASSENGER')")
@Tag(name = "Passenger Management", description = "Passenger-specific endpoints")
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping("/me")
    @Operation(summary = "Get current passenger profile")
    public ResponseEntity<PassengerResponse> getCurrentPassenger(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(passengerService.getPassengerByUserId(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update passenger profile")
    public ResponseEntity<PassengerResponse> updatePassengerProfile(
            Authentication authentication,
            @Valid @RequestBody PassengerService.UpdatePassengerRequest request) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(passengerService.updatePassenger(userId, request));
    }

    @GetMapping("/me/loyalty-points")
    @Operation(summary = "Get loyalty points")
    public ResponseEntity<Integer> getLoyaltyPoints(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(passengerService.getLoyaltyPoints(userId));
    }

    @PostMapping("/me/loyalty-points")
    @Operation(summary = "Add loyalty points")
    public ResponseEntity<Void> addLoyaltyPoints(
            Authentication authentication,
            @RequestParam Integer points) {
        Long userId = extractUserIdFromAuth(authentication);
        passengerService.addLoyaltyPoints(userId, points);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/preferred-language")
    @Operation(summary = "Update preferred language")
    public ResponseEntity<Void> updatePreferredLanguage(
            Authentication authentication,
            @RequestParam String language) {
        Long userId = extractUserIdFromAuth(authentication);
        passengerService.updatePreferredLanguage(userId, language);
        return ResponseEntity.ok().build();
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        return 1L; // Placeholder
    }
}