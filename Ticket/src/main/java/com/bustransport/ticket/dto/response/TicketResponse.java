package com.bustransport.ticket.dto.response;

import com.bustransport.ticket.enums.TicketStatus;
import com.bustransport.ticket.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private TicketType ticketType;
    private BigDecimal price;
    private LocalDateTime purchaseDate;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private TicketStatus status;
    private String qrCode;
    private Integer usageCount;
    private Integer maxUsage;
    private Long routeId;
    private Long scheduleId;
    private String passengerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
