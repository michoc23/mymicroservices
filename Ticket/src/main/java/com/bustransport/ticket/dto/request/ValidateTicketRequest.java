package com.bustransport.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTicketRequest {

    @NotBlank(message = "QR code is required")
    private String qrCode;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    private Long scheduleId;
}
