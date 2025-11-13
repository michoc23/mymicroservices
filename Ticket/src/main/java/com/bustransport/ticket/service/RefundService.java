package com.bustransport.ticket.service;

import com.bustransport.ticket.dto.request.CreateRefundRequest;
import com.bustransport.ticket.dto.response.RefundResponse;
import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.entity.Payment;
import com.bustransport.ticket.entity.Refund;
import com.bustransport.ticket.entity.Ticket;
import com.bustransport.ticket.enums.OrderStatus;
import com.bustransport.ticket.enums.RefundStatus;
import com.bustransport.ticket.enums.TicketStatus;
import com.bustransport.ticket.exception.RefundException;
import com.bustransport.ticket.exception.ResourceNotFoundException;
import com.bustransport.ticket.mapper.RefundMapper;
import com.bustransport.ticket.repository.PaymentRepository;
import com.bustransport.ticket.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RefundMapper refundMapper;

    @Transactional
    public RefundResponse createRefund(CreateRefundRequest request) {
        // Verify payment exists
        Payment payment = paymentRepository.findById(request.getPaymentId())
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));

        // Check if payment can be refunded
        if (!payment.canBeRefunded()) {
            throw new RefundException("Payment cannot be refunded. It may not be completed or refund period has expired");
        }

        // Calculate total refunded amount
        BigDecimal totalRefunded = payment.getTotalRefundedAmount();
        BigDecimal requestedRefund = request.getRefundAmount();
        BigDecimal remainingAmount = payment.getAmount().subtract(totalRefunded);

        // Validate refund amount
        if (requestedRefund.compareTo(remainingAmount) > 0) {
            throw new RefundException("Refund amount exceeds remaining payment amount");
        }

        // Determine if partial refund
        boolean isPartial = request.getIsPartial() != null ? request.getIsPartial()
            : requestedRefund.compareTo(remainingAmount) < 0;

        // Create refund
        Refund refund = Refund.builder()
            .payment(payment)
            .refundAmount(requestedRefund)
            .refundReason(request.getRefundReason())
            .refundStatus(RefundStatus.PENDING)
            .isPartial(isPartial)
            .build();

        refund = refundRepository.save(refund);

        // Process refund
        processRefund(refund, payment);

        return refundMapper.toResponse(refund);
    }

    @Transactional
    public void processRefund(Refund refund, Payment payment) {
        refund.markAsProcessing();
        refundRepository.save(refund);

        try {
            // Simulate refund processing with payment provider
            boolean refundSuccessful = processRefundWithProvider(refund, payment);

            if (refundSuccessful) {
                String transactionId = generateTransactionId();
                refund.markAsCompleted(transactionId);

                // Update payment status if fully refunded
                BigDecimal totalRefunded = payment.getTotalRefundedAmount().add(refund.getRefundAmount());
                if (totalRefunded.compareTo(payment.getAmount()) >= 0) {
                    payment.markAsRefunded();
                }

                // Update order status
                Order order = payment.getOrder();
                if (order != null) {
                    order.setStatus(OrderStatus.REFUNDED);

                    // Cancel all tickets
                    for (Ticket ticket : order.getTickets()) {
                        if (ticket.getStatus() != TicketStatus.USED) {
                            ticket.setStatus(TicketStatus.CANCELLED);
                        }
                    }
                }

                paymentRepository.save(payment);

                log.info("Refund processed successfully for payment: {}", payment.getId());
            } else {
                refund.markAsFailed("Refund processing failed with payment provider");
                log.error("Refund failed for payment: {}", payment.getId());
            }

            refundRepository.save(refund);
        } catch (Exception e) {
            refund.markAsFailed("Error processing refund: " + e.getMessage());
            refundRepository.save(refund);
            log.error("Error processing refund", e);
            throw new RefundException("Failed to process refund");
        }
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundById(Long id) {
        Refund refund = refundRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + id));
        return refundMapper.toResponse(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId).stream()
            .map(refundMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByUserId(Long userId) {
        return refundRepository.findByUserId(userId).stream()
            .map(refundMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void processPendingRefunds() {
        // This would be called by a scheduled job
        List<Refund> pendingRefunds = refundRepository.findByRefundStatus(RefundStatus.PENDING);

        for (Refund refund : pendingRefunds) {
            try {
                processRefund(refund, refund.getPayment());
            } catch (Exception e) {
                log.error("Error processing pending refund {}", refund.getId(), e);
            }
        }

        log.info("Processed {} pending refunds", pendingRefunds.size());
    }

    /**
     * Mock refund processing with payment provider
     * In production, this would integrate with Stripe, PayPal, etc.
     */
    private boolean processRefundWithProvider(Refund refund, Payment payment) {
        try {
            // Simulate refund processing delay
            Thread.sleep(1000);

            // Mock validation - 95% success rate
            return Math.random() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Refund processing interrupted", e);
            return false;
        }
    }

    private String generateTransactionId() {
        String transactionId;
        do {
            transactionId = "REF-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
        } while (refundRepository.existsByTransactionId(transactionId));
        return transactionId;
    }
}
