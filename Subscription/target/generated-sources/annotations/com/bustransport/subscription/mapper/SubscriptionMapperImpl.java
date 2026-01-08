package com.bustransport.subscription.mapper;

import com.bustransport.subscription.dto.request.CreateSubscriptionRequest;
import com.bustransport.subscription.dto.response.SubscriptionResponse;
import com.bustransport.subscription.entity.Subscription;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-19T16:51:59+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class SubscriptionMapperImpl implements SubscriptionMapper {

    @Override
    public Subscription toEntity(CreateSubscriptionRequest request) {
        if ( request == null ) {
            return null;
        }

        Subscription.SubscriptionBuilder subscription = Subscription.builder();

        subscription.autoRenewal( request.getAutoRenewal() );
        subscription.subscriptionType( request.getSubscriptionType() );
        subscription.userId( request.getUserId() );

        return subscription.build();
    }

    @Override
    public SubscriptionResponse toResponse(Subscription subscription) {
        if ( subscription == null ) {
            return null;
        }

        SubscriptionResponse.SubscriptionResponseBuilder subscriptionResponse = SubscriptionResponse.builder();

        subscriptionResponse.daysRemaining( subscription.getDaysRemaining() );
        subscriptionResponse.active( subscription.isActive() );
        subscriptionResponse.autoRenewal( subscription.getAutoRenewal() );
        subscriptionResponse.cancellationReason( subscription.getCancellationReason() );
        subscriptionResponse.cancelledAt( subscription.getCancelledAt() );
        subscriptionResponse.createdAt( subscription.getCreatedAt() );
        subscriptionResponse.endDate( subscription.getEndDate() );
        subscriptionResponse.id( subscription.getId() );
        subscriptionResponse.paymentId( subscription.getPaymentId() );
        subscriptionResponse.price( subscription.getPrice() );
        subscriptionResponse.startDate( subscription.getStartDate() );
        subscriptionResponse.status( subscription.getStatus() );
        subscriptionResponse.subscriptionType( subscription.getSubscriptionType() );
        subscriptionResponse.updatedAt( subscription.getUpdatedAt() );
        subscriptionResponse.userId( subscription.getUserId() );

        return subscriptionResponse.build();
    }
}
