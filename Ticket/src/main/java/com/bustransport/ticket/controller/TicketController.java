package com.bustransport.ticket.controller;

import com.bustransport.ticket.dto.request.ValidateTicketRequest;
import com.bustransport.ticket.dto.response.TicketResponse;
import com.bustransport.ticket.dto.response.TicketValidationResponse;
import com.bustransport.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket", description = "Ticket management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        TicketResponse response = ticketService.getTicketById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/qr/{qrCode}")
    @Operation(summary = "Get ticket by QR code")
    public ResponseEntity<TicketResponse> getTicketByQrCode(@PathVariable String qrCode) {
        TicketResponse response = ticketService.getTicketByQrCode(qrCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all tickets for a user")
    public ResponseEntity<List<TicketResponse>> getTicketsByUserId(@PathVariable Long userId) {
        List<TicketResponse> responses = ticketService.getTicketsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active tickets for a user")
    public ResponseEntity<List<TicketResponse>> getActiveTicketsByUserId(@PathVariable Long userId) {
        List<TicketResponse> responses = ticketService.getActiveTicketsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate and use a ticket")
    public ResponseEntity<TicketValidationResponse> validateAndUseTicket(@Valid @RequestBody ValidateTicketRequest request) {
        TicketValidationResponse response = ticketService.validateAndUseTicket(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a ticket")
    public ResponseEntity<TicketResponse> cancelTicket(@PathVariable Long id) {
        TicketResponse response = ticketService.cancelTicket(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/qr-image")
    @Operation(summary = "Get QR code image for a ticket")
    public ResponseEntity<byte[]> getTicketQRCodeImage(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketById(id);
        String base64Image = ticketService.generateQRCodeImage(ticket.getQrCode());
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(imageBytes);
    }
}
