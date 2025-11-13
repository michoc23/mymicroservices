package com.bustransport.ticket.service;

import com.bustransport.ticket.dto.request.CreatePaymentRequest;
import com.bustransport.ticket.dto.response.PaymentResponse;
import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.entity.Payment;
import com.bustransport.ticket.enums.PaymentStatus;
import com.bustransport.ticket.exception.PaymentFailedException;
import com.bustransport.ticket.exception.ResourceNotFoundException;
import com.bustransport.ticket.mapper.PaymentMapper;
import com.bustransport.ticket.repository.OrderRepository;
import com.bustransport.ticket.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Verify order exists
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalStateException("Payment already exists for this order");
        }

        // Verify amount matches order total
        if (order.getTotalAmount().compareTo(request.getAmount()) != 0) {
            throw new PaymentFailedException("Payment amount does not match order total");
        }

        // Create payment
        Payment payment = Payment.builder()
            .order(order)
            .userId(request.getUserId())
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod())
            .status(PaymentStatus.PENDING)
            .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
            .build();

        payment = paymentRepository.save(payment);

        // Process payment with payment provider
        boolean paymentSuccessful = processPaymentWithProvider(request, payment);

        if (paymentSuccessful) {
            String transactionId = generateTransactionId();
            payment.markAsCompleted(transactionId);

            // Mark order as paid
            orderService.markOrderAsPaid(order.getId());

            log.info("Payment completed successfully for order: {}", order.getOrderNumber());
        } else {
            payment.markAsFailed("Payment processing failed");
            log.error("Payment failed for order: {}", order.getOrderNumber());
            throw new PaymentFailedException("Payment processing failed");
        }

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction id: " + transactionId));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
            .map(paymentMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Mock payment processing with payment provider
     * In production, this would integrate with Stripe, PayPal, etc.
     */
    private boolean processPaymentWithProvider(CreatePaymentRequest request, Payment payment) {
        try {
            // Simulate payment processing delay
            Thread.sleep(1000);

            // Mock payment validation
            switch (request.getPaymentMethod()) {
                case CREDIT_CARD:
                case DEBIT_CARD:
                    return validateCardPayment(request);
                case PAYPAL:
                    return validatePayPalPayment(request);
                case WALLET:
                    return true; // Assume wallet has sufficient balance
                case CASH:
                    return true; // Cash payment confirmed
                default:
                    return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
            return false;
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return false;
        }
    }

    private boolean validateCardPayment(CreatePaymentRequest request) {
        // Mock validation - in production would call payment gateway
        return request.getCardNumber() != null
            && request.getCardHolderName() != null
            && request.getExpiryDate() != null
            && request.getCvv() != null
            && request.getCardNumber().length() >= 13;
    }

    private boolean validatePayPalPayment(CreatePaymentRequest request) {
        // Mock validation - in production would call PayPal API
        return request.getPaypalEmail() != null && request.getPaypalEmail().contains("@");
    }

    private String generateTransactionId() {
        String transactionId;
        do {
            transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
        } while (paymentRepository.existsByTransactionId(transactionId));
        return transactionId;
    }
}
