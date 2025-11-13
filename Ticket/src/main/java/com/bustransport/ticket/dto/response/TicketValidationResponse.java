package com.bustransport.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketValidationResponse {

    private boolean valid;
    private String message;
    private TicketResponse ticket;
}
