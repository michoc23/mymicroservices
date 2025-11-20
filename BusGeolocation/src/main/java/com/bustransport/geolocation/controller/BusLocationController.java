package com.bustransport.geolocation.controller;

import com.bustransport.geolocation.dto.request.TelemetryIngestDTO;
import com.bustransport.geolocation.dto.response.BusLocationDTO;
import com.bustransport.geolocation.service.BusLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Tag(name = "Bus Locations", description = "Bus location tracking and telemetry APIs")
public class BusLocationController {

    private final BusLocationService locationService;

    @PostMapping("/ingest")
    @Operation(summary = "Ingest GPS telemetry data")
    public ResponseEntity<BusLocationDTO> ingestTelemetry(@Valid @RequestBody TelemetryIngestDTO telemetry) {
        return ResponseEntity.ok(locationService.ingestTelemetry(telemetry));
    }

    @GetMapping("/bus/{busId}/current")
    @Operation(summary = "Get current location of a bus")
    public ResponseEntity<BusLocationDTO> getCurrentLocation(@PathVariable Long busId) {
        return ResponseEntity.ok(locationService.getCurrentLocation(busId));
    }

    @GetMapping("/bus/{busId}/history")
    @Operation(summary = "Get location history for a bus")
    public ResponseEntity<List<BusLocationDTO>> getLocationHistory(
            @PathVariable Long busId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(locationService.getLocationHistory(busId, startTime, endTime));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active bus locations")
    public ResponseEntity<List<BusLocationDTO>> getAllActiveBusLocations() {
        return ResponseEntity.ok(locationService.getAllActiveBusLocations());
    }
}

