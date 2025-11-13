package com.bustransport.ticket.dto.response;

import com.bustransport.ticket.enums.RefundStatus;
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
public class RefundResponse {

    private Long id;
    private Long paymentId;
    private BigDecimal refundAmount;
    private String refundReason;
    private RefundStatus refundStatus;
    private String transactionId;
    private Boolean isPartial;
    private LocalDateTime refundDate;
    private String processedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
