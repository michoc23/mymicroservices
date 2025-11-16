package com.bustransport.subscription.dto.response;

import com.bustransport.subscription.enums.SubscriptionStatus;
import com.bustransport.subscription.enums.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private SubscriptionType subscriptionType;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubscriptionStatus status;
    private Boolean autoRenewal;
    private Long paymentId;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private long daysRemaining;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}