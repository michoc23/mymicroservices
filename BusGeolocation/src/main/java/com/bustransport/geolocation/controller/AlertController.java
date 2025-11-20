package com.bustransport.geolocation.controller;

import com.bustransport.geolocation.dto.response.AlertDTO;
import com.bustransport.geolocation.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Geofence and bus alert management APIs")
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/active")
    @Operation(summary = "Get all active alerts")
    public ResponseEntity<List<AlertDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/bus/{busId}")
    @Operation(summary = "Get alerts for a specific bus")
    public ResponseEntity<List<AlertDTO>> getAlertsByBus(@PathVariable Long busId) {
        return ResponseEntity.ok(alertService.getAlertsByBus(busId));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent alerts")
    public ResponseEntity<List<AlertDTO>> getRecentAlerts(@RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(alertService.getRecentAlerts(hours));
    }

    @PutMapping("/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<AlertDTO> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestParam String acknowledgedBy,
            @RequestParam(required = false) String resolution) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(alertId, acknowledgedBy, resolution));
    }
}

