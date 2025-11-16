package com.bustransport.subscription.mapper;

import com.bustransport.subscription.dto.request.CreateSubscriptionRequest;
import com.bustransport.subscription.dto.response.SubscriptionResponse;
import com.bustransport.subscription.entity.Subscription;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subscription toEntity(CreateSubscriptionRequest request);

    @Mapping(target = "daysRemaining", source = "daysRemaining")
    @Mapping(target = "active", source = "active")
    SubscriptionResponse toResponse(Subscription subscription);

    @AfterMapping
    default void setCalculatedFields(@MappingTarget SubscriptionResponse response, Subscription subscription) {
        response.setDaysRemaining(subscription.getDaysRemaining());
        response.setActive(subscription.isActive());
    }
}