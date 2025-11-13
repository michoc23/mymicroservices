package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.RefundResponse;
import com.bustransport.ticket.entity.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    RefundResponse toResponse(Refund refund);
}
