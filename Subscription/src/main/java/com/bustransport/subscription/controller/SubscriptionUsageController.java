package com.bustransport.subscription.controller;

import com.bustransport.subscription.entity.SubscriptionUsage;
import com.bustransport.subscription.service.SubscriptionUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-usage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Usage", description = "APIs for tracking subscription usage")
public class SubscriptionUsageController {

    private final SubscriptionUsageService subscriptionUsageService;

    @Operation(summary = "Record subscription usage")
    @PostMapping("/record")
    public ResponseEntity<SubscriptionUsage> recordUsage(
            @Parameter(description = "Subscription ID") @RequestParam Long subscriptionId,
            @Parameter(description = "Route ID") @RequestParam Long routeId,
            @Parameter(description = "Bus ID") @RequestParam Long busId) {
        
        log.info("Recording usage for subscription: {} on route: {} and bus: {}", subscriptionId, routeId, busId);
        SubscriptionUsage usage = subscriptionUsageService.recordUsage(subscriptionId, routeId, busId);
        return ResponseEntity.status(HttpStatus.CREATED).body(usage);
    }

    @Operation(summary = "Get subscription usage history")
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<SubscriptionUsage>> getSubscriptionUsage(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId) {
        
        log.info("Getting usage for subscription: {}", subscriptionId);
        List<SubscriptionUsage> usage = subscriptionUsageService.getUsageBySubscriptionId(subscriptionId);
        return ResponseEntity.ok(usage);
    }

    @Operation(summary = "Get paginated subscription usage history")
    @GetMapping("/subscription/{subscriptionId}/paginated")
    public ResponseEntity<Page<SubscriptionUsage>> getSubscriptionUsagePaginated(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting paginated usage for subscription: {}", subscriptionId);
        Page<SubscriptionUsage> usage = subscriptionUsageService.getUsageBySubscriptionId(subscriptionId, pageable);
        return ResponseEntity.ok(usage);
    }

    @Operation(summary = "Get subscription usage by date range")
    @GetMapping("/subscription/{subscriptionId}/range")
    public ResponseEntity<List<SubscriptionUsage>> getUsageByDateRange(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId,
            @Parameter(description = "Start date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Getting usage for subscription: {} between {} and {}", subscriptionId, startDate, endDate);
        List<SubscriptionUsage> usage = subscriptionUsageService.getUsageByDateRange(subscriptionId, startDate, endDate);
        return ResponseEntity.ok(usage);
    }

    @Operation(summary = "Get subscription usage count by date range")
    @GetMapping("/subscription/{subscriptionId}/count")
    public ResponseEntity<Integer> getUsageCountByDateRange(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId,
            @Parameter(description = "Start date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Getting usage count for subscription: {} between {} and {}", subscriptionId, startDate, endDate);
        int count = subscriptionUsageService.getUsageCountByDateRange(subscriptionId, startDate, endDate);
        return ResponseEntity.ok(count);
    }
}