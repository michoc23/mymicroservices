package com.bustransport.user.controller;

import com.bustransport.user.dto.DriverResponse;
import com.bustransport.user.service.DriverService;
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
@RequestMapping("/drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "Driver Management", description = "Driver-specific endpoints")
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/me")
    @Operation(summary = "Get current driver profile")
    public ResponseEntity<DriverResponse> getCurrentDriver(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(driverService.getDriverByUserId(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update driver profile")
    public ResponseEntity<DriverResponse> updateDriverProfile(
            Authentication authentication,
            @Valid @RequestBody DriverService.UpdateDriverRequest request) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(driverService.updateDriver(userId, request));
    }

    @PostMapping("/me/start-shift")
    @Operation(summary = "Start driver shift")
    public ResponseEntity<Void> startShift(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        driverService.startShift(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/end-shift")
    @Operation(summary = "End driver shift")
    public ResponseEntity<Void> endShift(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        driverService.endShift(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/status")
    @Operation(summary = "Get driver status")
    public ResponseEntity<String> getDriverStatus(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(driverService.getDriverStatus(userId));
    }

    @PutMapping("/me/bus")
    @Operation(summary = "Assign bus to driver")
    public ResponseEntity<Void> assignBus(
            Authentication authentication,
            @RequestParam Long busId) {
        Long userId = extractUserIdFromAuth(authentication);
        driverService.assignBus(userId, busId);
        return ResponseEntity.ok().build();
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        return 1L; // Placeholder
    }
}