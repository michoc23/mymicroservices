package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.OrderResponse;
import com.bustransport.ticket.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TicketMapper.class, PaymentMapper.class})
public interface OrderMapper {

    @Mapping(target = "tickets", source = "tickets")
    @Mapping(target = "payment", source = "payment")
    OrderResponse toResponse(Order order);
}
