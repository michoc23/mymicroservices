package com.bustransport.ticket.dto.request;

import com.bustransport.ticket.enums.TicketType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotNull(message = "Ticket type is required")
    private TicketType ticketType;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    private Long scheduleId;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    private String passengerName;

    private Integer maxUsage;
}
