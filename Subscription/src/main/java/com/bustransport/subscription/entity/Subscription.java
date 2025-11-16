package com.bustransport.subscription.entity;

import com.bustransport.subscription.enums.SubscriptionStatus;
import com.bustransport.subscription.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Builder.Default
    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = false;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE 
            && LocalDateTime.now().isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate) 
            || status == SubscriptionStatus.EXPIRED;
    }

    public void cancel(String reason) {
        if (status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Subscription is already cancelled");
        }
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.autoRenewal = false;
    }

    public void renew() {
        if (status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot renew cancelled subscription");
        }
        
        LocalDateTime newStartDate = this.endDate;
        LocalDateTime newEndDate;
        
        if (subscriptionType == SubscriptionType.MONTHLY) {
            newEndDate = newStartDate.plusMonths(1);
        } else {
            newEndDate = newStartDate.plusYears(1);
        }
        
        this.startDate = newStartDate;
        this.endDate = newEndDate;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void suspend() {
        if (status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot suspend cancelled subscription");
        }
        this.status = SubscriptionStatus.SUSPENDED;
    }

    public void reactivate() {
        if (status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reactivate cancelled subscription");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot reactivate expired subscription");
        }
        this.status = SubscriptionStatus.ACTIVE;
    }

    public boolean canBeCancelled() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.SUSPENDED;
    }

    public long getDaysRemaining() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
    }
}