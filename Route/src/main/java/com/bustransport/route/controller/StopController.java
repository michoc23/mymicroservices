package com.bustransport.route.controller;

import com.bustransport.route.dto.response.StopDTO;
import com.bustransport.route.service.StopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/stops")
@RequiredArgsConstructor
@Tag(name = "Stops", description = "Stop management APIs")
public class StopController {

    private final StopService stopService;

    @GetMapping
    @Operation(summary = "List all stops")
    public ResponseEntity<Page<StopDTO>> listStops(
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        return ResponseEntity.ok(stopService.listStops(isActive, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stop by ID")
    public ResponseEntity<StopDTO> getStopById(@PathVariable Long id) {
        return ResponseEntity.ok(stopService.getStopById(id));
    }

    @GetMapping("/code/{stopCode}")
    @Operation(summary = "Get stop by code")
    public ResponseEntity<StopDTO> getStopByCode(@PathVariable String stopCode) {
        return ResponseEntity.ok(stopService.getStopByCode(stopCode));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby stops")
    public ResponseEntity<List<StopDTO>> findNearbyStops(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "1.0") double radiusKm) {
        return ResponseEntity.ok(stopService.findNearbyStops(latitude, longitude, radiusKm));
    }

    @GetMapping("/search")
    @Operation(summary = "Search stops")
    public ResponseEntity<List<StopDTO>> searchStops(@RequestParam String query) {
        return ResponseEntity.ok(stopService.searchStops(query));
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Get stops for a route")
    public ResponseEntity<List<StopDTO>> getStopsByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(stopService.getStopsByRoute(routeId));
    }
}

