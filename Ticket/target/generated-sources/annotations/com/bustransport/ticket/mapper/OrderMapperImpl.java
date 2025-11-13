package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.OrderResponse;
import com.bustransport.ticket.dto.response.TicketResponse;
import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.entity.Ticket;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-13T22:34:15+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private TicketMapper ticketMapper;
    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.tickets( ticketListToTicketResponseList( order.getTickets() ) );
        orderResponse.payment( paymentMapper.toResponse( order.getPayment() ) );
        orderResponse.createdAt( order.getCreatedAt() );
        orderResponse.id( order.getId() );
        orderResponse.orderNumber( order.getOrderNumber() );
        orderResponse.status( order.getStatus() );
        orderResponse.totalAmount( order.getTotalAmount() );
        orderResponse.updatedAt( order.getUpdatedAt() );
        orderResponse.userId( order.getUserId() );

        return orderResponse.build();
    }

    protected List<TicketResponse> ticketListToTicketResponseList(List<Ticket> list) {
        if ( list == null ) {
            return null;
        }

        List<TicketResponse> list1 = new ArrayList<TicketResponse>( list.size() );
        for ( Ticket ticket : list ) {
            list1.add( ticketMapper.toResponse( ticket ) );
        }

        return list1;
    }
}
