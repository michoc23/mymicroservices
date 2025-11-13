package com.bustransport.ticket.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "At least one ticket is required")
    @Valid
    private List<CreateTicketRequest> tickets;
}
