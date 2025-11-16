package com.bustransport.subscription.dto.request;

import com.bustransport.subscription.enums.SubscriptionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Subscription type is required")
    private SubscriptionType subscriptionType;

    @Builder.Default
    private Boolean autoRenewal = false;
}