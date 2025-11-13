package com.bustransport.ticket.mapper;

import com.bustransport.ticket.dto.response.RefundResponse;
import com.bustransport.ticket.entity.Payment;
import com.bustransport.ticket.entity.Refund;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-13T22:34:15+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class RefundMapperImpl implements RefundMapper {

    @Override
    public RefundResponse toResponse(Refund refund) {
        if ( refund == null ) {
            return null;
        }

        RefundResponse.RefundResponseBuilder refundResponse = RefundResponse.builder();

        refundResponse.paymentId( refundPaymentId( refund ) );
        refundResponse.createdAt( refund.getCreatedAt() );
        refundResponse.id( refund.getId() );
        refundResponse.isPartial( refund.getIsPartial() );
        refundResponse.processedBy( refund.getProcessedBy() );
        refundResponse.refundAmount( refund.getRefundAmount() );
        refundResponse.refundDate( refund.getRefundDate() );
        refundResponse.refundReason( refund.getRefundReason() );
        refundResponse.refundStatus( refund.getRefundStatus() );
        refundResponse.transactionId( refund.getTransactionId() );
        refundResponse.updatedAt( refund.getUpdatedAt() );

        return refundResponse.build();
    }

    private Long refundPaymentId(Refund refund) {
        if ( refund == null ) {
            return null;
        }
        Payment payment = refund.getPayment();
        if ( payment == null ) {
            return null;
        }
        Long id = payment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
