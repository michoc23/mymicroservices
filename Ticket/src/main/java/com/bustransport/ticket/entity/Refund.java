package com.bustransport.ticket.entity;

import com.bustransport.ticket.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(length = 500)
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus refundStatus;

    @Column(unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPartial = false;

    private LocalDateTime refundDate;

    @Column(length = 100)
    private String processedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods
    public void markAsCompleted(String transactionId) {
        this.refundStatus = RefundStatus.COMPLETED;
        this.transactionId = transactionId;
        this.refundDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.refundStatus = RefundStatus.FAILED;
        this.refundReason = (this.refundReason != null ? this.refundReason + " | " : "") + reason;
    }

    public void markAsProcessing() {
        this.refundStatus = RefundStatus.PROCESSING;
    }

    public boolean isPending() {
        return refundStatus == RefundStatus.PENDING;
    }

    public boolean isCompleted() {
        return refundStatus == RefundStatus.COMPLETED;
    }
}
