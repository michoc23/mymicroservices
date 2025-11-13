package com.bustransport.ticket.entity;

import com.bustransport.ticket.enums.PaymentMethod;
import com.bustransport.ticket.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long subscriptionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Refund> refunds = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods
    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED
            && LocalDateTime.now().isBefore(paymentDate.plusDays(30));
    }

    public void markAsCompleted(String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.providerResponse = reason;
    }

    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public BigDecimal getTotalRefundedAmount() {
        return refunds.stream()
            .filter(refund -> refund.getRefundStatus() == com.bustransport.ticket.enums.RefundStatus.COMPLETED)
            .map(Refund::getRefundAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Helper method to add refund
    public void addRefund(Refund refund) {
        refunds.add(refund);
        refund.setPayment(this);
    }

    // Helper method to remove refund
    public void removeRefund(Refund refund) {
        refunds.remove(refund);
        refund.setPayment(null);
    }
}
