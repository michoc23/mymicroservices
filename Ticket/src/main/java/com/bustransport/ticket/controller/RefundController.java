package com.bustransport.ticket.controller;

import com.bustransport.ticket.dto.request.CreateRefundRequest;
import com.bustransport.ticket.dto.response.RefundResponse;
import com.bustransport.ticket.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "Refund processing APIs")
@SecurityRequirement(name = "bearer-jwt")
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    @Operation(summary = "Create a new refund request")
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody CreateRefundRequest request) {
        RefundResponse response = refundService.createRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get refund by ID")
    public ResponseEntity<RefundResponse> getRefundById(@PathVariable Long id) {
        RefundResponse response = refundService.getRefundById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Get all refunds for a payment")
    public ResponseEntity<List<RefundResponse>> getRefundsByPaymentId(@PathVariable Long paymentId) {
        List<RefundResponse> responses = refundService.getRefundsByPaymentId(paymentId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all refunds for a user")
    public ResponseEntity<List<RefundResponse>> getRefundsByUserId(@PathVariable Long userId) {
        List<RefundResponse> responses = refundService.getRefundsByUserId(userId);
        return ResponseEntity.ok(responses);
    }
}
