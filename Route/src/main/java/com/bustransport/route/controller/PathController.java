package com.bustransport.route.controller;

import com.bustransport.route.dto.request.OptimalPathRequest;
import com.bustransport.route.dto.response.OptimalPathResponse;
import com.bustransport.route.service.OptimalPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paths")
@RequiredArgsConstructor
@Tag(name = "Path Planning", description = "Optimal path and route planning APIs")
public class PathController {

    private final OptimalPathService optimalPathService;

    @PostMapping("/optimal")
    @Operation(summary = "Calculate optimal path between two points")
    public ResponseEntity<OptimalPathResponse> calculateOptimalPath(
            @Valid @RequestBody OptimalPathRequest request) {
        return ResponseEntity.ok(optimalPathService.calculateOptimalPath(request));
    }
}

