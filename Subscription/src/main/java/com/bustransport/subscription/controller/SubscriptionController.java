package com.bustransport.subscription.controller;

import com.bustransport.subscription.dto.request.CreateSubscriptionRequest;
import com.bustransport.subscription.dto.request.UpdateSubscriptionRequest;
import com.bustransport.subscription.dto.request.CancelSubscriptionRequest;
import com.bustransport.subscription.dto.response.SubscriptionResponse;
import com.bustransport.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Get subscription by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        log.info("Getting subscription with id: {}", id);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Get all subscriptions for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Getting subscriptions for user: {}", userId);
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Get paginated subscriptions for a user")
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<SubscriptionResponse>> getUserSubscriptionsPaginated(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("Getting paginated subscriptions for user: {}", userId);
        Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Get active subscription for a user")
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Getting active subscription for user: {}", userId);
        Optional<SubscriptionResponse> subscription = subscriptionService.getActiveSubscriptionByUserId(userId);
        return subscription.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new subscription")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user already has active subscription")
    })
    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        
        log.info("Creating subscription for user: {}", request.getUserId());
        SubscriptionResponse subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @Operation(summary = "Update subscription settings")
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        
        log.info("Updating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Renew a subscription")
    @PostMapping("/{id}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        log.info("Renewing subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.renewSubscription(id);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Cancel a subscription")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @Valid @RequestBody CancelSubscriptionRequest request) {
        
        log.info("Cancelling subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.cancelSubscription(id, request.getCancellationReason());
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Suspend a subscription")
    @PostMapping("/{id}/suspend")
    public ResponseEntity<SubscriptionResponse> suspendSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        log.info("Suspending subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.suspendSubscription(id);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Reactivate a subscription")
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<SubscriptionResponse> reactivateSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        log.info("Reactivating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.reactivateSubscription(id);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Check if user has active subscription")
    @GetMapping("/user/{userId}/has-active")
    public ResponseEntity<Boolean> hasActiveSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Checking active subscription for user: {}", userId);
        boolean hasActive = subscriptionService.hasActiveSubscription(userId);
        return ResponseEntity.ok(hasActive);
    }
}