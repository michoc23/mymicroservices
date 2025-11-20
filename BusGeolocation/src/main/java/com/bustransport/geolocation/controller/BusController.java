package com.bustransport.geolocation.controller;

import com.bustransport.geolocation.dto.response.BusLocationDTO;
import com.bustransport.geolocation.entity.Bus;
import com.bustransport.geolocation.repository.BusRepository;
import com.bustransport.geolocation.service.BusLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buses")
@RequiredArgsConstructor
@Tag(name = "Buses", description = "Bus management APIs")
public class BusController {

    private final BusRepository busRepository;
    private final BusLocationService busLocationService;

    @GetMapping
    @Operation(summary = "Get all buses")
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(busRepository.findAll());
    }

    @GetMapping("/{busId}")
    @Operation(summary = "Get bus by ID")
    public ResponseEntity<Bus> getBusById(@PathVariable Long busId) {
        return busRepository.findById(busId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{busId}/location")
    @Operation(summary = "Get current location of a bus")
    public ResponseEntity<BusLocationDTO> getBusLocation(@PathVariable Long busId) {
        return ResponseEntity.ok(busLocationService.getCurrentLocation(busId));
    }

    @GetMapping("/route")
    @Operation(summary = "Get buses on a route")
    public ResponseEntity<List<Bus>> getBusesByRoute(@RequestParam Long routeId) {
        return ResponseEntity.ok(busRepository.findByRouteId(routeId));
    }
}

