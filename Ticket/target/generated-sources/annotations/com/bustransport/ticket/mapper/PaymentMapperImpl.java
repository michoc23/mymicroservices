package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.PaymentResponse;
import com.bustransport.ticket.dto.response.RefundResponse;
import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.entity.Payment;
import com.bustransport.ticket.entity.Refund;
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
public class PaymentMapperImpl implements PaymentMapper {

    @Autowired
    private RefundMapper refundMapper;

    @Override
    public PaymentResponse toResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder paymentResponse = PaymentResponse.builder();

        paymentResponse.orderId( paymentOrderId( payment ) );
        paymentResponse.refunds( refundListToRefundResponseList( payment.getRefunds() ) );
        paymentResponse.amount( payment.getAmount() );
        paymentResponse.createdAt( payment.getCreatedAt() );
        paymentResponse.currency( payment.getCurrency() );
        paymentResponse.id( payment.getId() );
        paymentResponse.paymentDate( payment.getPaymentDate() );
        paymentResponse.paymentMethod( payment.getPaymentMethod() );
        paymentResponse.status( payment.getStatus() );
        paymentResponse.subscriptionId( payment.getSubscriptionId() );
        paymentResponse.transactionId( payment.getTransactionId() );
        paymentResponse.updatedAt( payment.getUpdatedAt() );
        paymentResponse.userId( payment.getUserId() );

        return paymentResponse.build();
    }

    private Long paymentOrderId(Payment payment) {
        if ( payment == null ) {
            return null;
        }
        Order order = payment.getOrder();
        if ( order == null ) {
            return null;
        }
        Long id = order.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<RefundResponse> refundListToRefundResponseList(List<Refund> list) {
        if ( list == null ) {
            return null;
        }

        List<RefundResponse> list1 = new ArrayList<RefundResponse>( list.size() );
        for ( Refund refund : list ) {
            list1.add( refundMapper.toResponse( refund ) );
        }

        return list1;
    }
}
