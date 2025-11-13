package com.bustransport.ticket.dto.response;

import com.bustransport.ticket.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<TicketResponse> tickets;
    private PaymentResponse payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
