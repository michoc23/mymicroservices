package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.PaymentResponse;
import com.bustransport.ticket.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RefundMapper.class})
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "refunds", source = "refunds")
    PaymentResponse toResponse(Payment payment);
}
