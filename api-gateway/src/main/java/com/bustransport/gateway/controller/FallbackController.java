package com.bustransport.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return createFallbackResponse("User Service");
    }

    @GetMapping("/bus-service")
    public ResponseEntity<Map<String, Object>> busServiceFallback() {
        return createFallbackResponse("Bus Service");
    }

    @GetMapping("/booking-service")
    public ResponseEntity<Map<String, Object>> bookingServiceFallback() {
        return createFallbackResponse("Booking Service");
    }

    @GetMapping("/payment-service")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createFallbackResponse("Payment Service");
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return createFallbackResponse("Notification Service");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        response.put("service", serviceName);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
