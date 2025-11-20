package com.bustransport.route.controller;

import com.bustransport.route.dto.response.RouteDTO;
import com.bustransport.route.enums.RouteType;
import com.bustransport.route.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Route management APIs")
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    @Operation(summary = "List all routes")
    public ResponseEntity<Page<RouteDTO>> listRoutes(
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        return ResponseEntity.ok(routeService.listRoutes(isActive, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID")
    public ResponseEntity<RouteDTO> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/number/{routeNumber}")
    @Operation(summary = "Get route by route number")
    public ResponseEntity<RouteDTO> getRouteByNumber(@PathVariable String routeNumber) {
        return ResponseEntity.ok(routeService.getRouteByNumber(routeNumber));
    }

    @GetMapping("/type/{routeType}")
    @Operation(summary = "Get routes by type")
    public ResponseEntity<List<RouteDTO>> getRoutesByType(@PathVariable RouteType routeType) {
        return ResponseEntity.ok(routeService.getRoutesByType(routeType));
    }

    @GetMapping("/search")
    @Operation(summary = "Search routes")
    public ResponseEntity<List<RouteDTO>> searchRoutes(@RequestParam String query) {
        return ResponseEntity.ok(routeService.searchRoutes(query));
    }

    @GetMapping("/stop/{stopId}")
    @Operation(summary = "Get routes serving a stop")
    public ResponseEntity<List<RouteDTO>> getRoutesByStop(@PathVariable Long stopId) {
        return ResponseEntity.ok(routeService.getRoutesByStopId(stopId));
    }
}

