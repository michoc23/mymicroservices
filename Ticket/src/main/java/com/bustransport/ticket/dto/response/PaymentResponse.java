package com.bustransport.ticket.dto.response;

import com.bustransport.ticket.enums.PaymentMethod;
import com.bustransport.ticket.enums.PaymentStatus;
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
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private Long subscriptionId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String currency;
    private List<RefundResponse> refunds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
