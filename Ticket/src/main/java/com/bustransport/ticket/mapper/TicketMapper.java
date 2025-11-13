package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.TicketResponse;
import com.bustransport.ticket.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "orderId", source = "order.id")
    TicketResponse toResponse(Ticket ticket);
}
