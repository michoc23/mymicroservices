package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.TicketResponse;
import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.entity.Ticket;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-13T22:34:15+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class TicketMapperImpl implements TicketMapper {

    @Override
    public TicketResponse toResponse(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }

        TicketResponse.TicketResponseBuilder ticketResponse = TicketResponse.builder();

        ticketResponse.orderId( ticketOrderId( ticket ) );
        ticketResponse.createdAt( ticket.getCreatedAt() );
        ticketResponse.id( ticket.getId() );
        ticketResponse.maxUsage( ticket.getMaxUsage() );
        ticketResponse.passengerName( ticket.getPassengerName() );
        ticketResponse.price( ticket.getPrice() );
        ticketResponse.purchaseDate( ticket.getPurchaseDate() );
        ticketResponse.qrCode( ticket.getQrCode() );
        ticketResponse.routeId( ticket.getRouteId() );
        ticketResponse.scheduleId( ticket.getScheduleId() );
        ticketResponse.status( ticket.getStatus() );
        ticketResponse.ticketType( ticket.getTicketType() );
        ticketResponse.updatedAt( ticket.getUpdatedAt() );
        ticketResponse.usageCount( ticket.getUsageCount() );
        ticketResponse.userId( ticket.getUserId() );
        ticketResponse.validFrom( ticket.getValidFrom() );
        ticketResponse.validUntil( ticket.getValidUntil() );

        return ticketResponse.build();
    }

    private Long ticketOrderId(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Order order = ticket.getOrder();
        if ( order == null ) {
            return null;
        }
        Long id = order.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
