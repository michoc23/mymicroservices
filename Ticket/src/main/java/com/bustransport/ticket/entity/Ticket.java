package com.bustransport.ticket.entity;

import com.bustransport.ticket.enums.TicketStatus;
import com.bustransport.ticket.enums.TicketType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType ticketType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(nullable = false, unique = true)
    private String qrCode;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxUsage = 1;

    @Column(nullable = false)
    private Long routeId;

    private Long scheduleId;

    @Column(length = 100)
    private String passengerName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods
    public boolean isValid() {
        return status == TicketStatus.ACTIVE
            && LocalDateTime.now().isBefore(validUntil)
            && usageCount < maxUsage;
    }

    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("Ticket is not valid for use");
        }
        this.usageCount++;
        if (this.usageCount >= this.maxUsage) {
            this.status = TicketStatus.USED;
        }
    }

    public void cancel() {
        if (status == TicketStatus.USED) {
            throw new IllegalStateException("Cannot cancel used ticket");
        }
        this.status = TicketStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return status == TicketStatus.ACTIVE
            && usageCount == 0
            && LocalDateTime.now().isBefore(validFrom.minusHours(2));
    }
}
