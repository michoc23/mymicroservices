package com.bustransport.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for ticket consumption check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketConsumptionResponse {
    private Long ticketId;
    private String qrCode;
    private String status; // ACTIVE, USED, CANCELLED, EXPIRED
    private Boolean isConsumed;
    private Boolean isValid;
    private Integer usageCount;
    private Integer maxUsage;
    private LocalDateTime validUntil;
    private String message;
    private Long userId;
    private Long routeId;
}
