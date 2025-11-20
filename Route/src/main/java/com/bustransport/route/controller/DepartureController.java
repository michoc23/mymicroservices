package com.bustransport.route.controller;

import com.bustransport.route.dto.request.NextDeparturesRequest;
import com.bustransport.route.dto.response.DepartureDTO;
import com.bustransport.route.dto.response.NextDeparturesResponse;
import com.bustransport.route.enums.DepartureStatus;
import com.bustransport.route.service.DepartureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/departures")
@RequiredArgsConstructor
@Tag(name = "Departures", description = "Departure and real-time information APIs")
public class DepartureController {

    private final DepartureService departureService;

    @PostMapping("/next")
    @Operation(summary = "Get next departures for a stop")
    public ResponseEntity<NextDeparturesResponse> getNextDepartures(
            @RequestBody NextDeparturesRequest request) {
        return ResponseEntity.ok(departureService.getNextDepartures(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get departure by ID")
    public ResponseEntity<DepartureDTO> getDepartureById(@PathVariable Long id) {
        return ResponseEntity.ok(departureService.getDepartureById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get departures by status")
    public ResponseEntity<List<DepartureDTO>> getDeparturesByStatus(@PathVariable DepartureStatus status) {
        return ResponseEntity.ok(departureService.getDeparturesByStatus(status));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update departure status")
    public ResponseEntity<DepartureDTO> updateDepartureStatus(
            @PathVariable Long id,
            @RequestParam DepartureStatus status,
            @RequestParam(required = false) Integer delayMinutes,
            @RequestParam(required = false) String message) {
        return ResponseEntity.ok(departureService.updateDepartureStatus(id, status, delayMinutes, message));
    }
}

