package com.bustransport.route.entity;

import com.bustransport.route.enums.DepartureStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "departures", indexes = {
    @Index(name = "idx_departure_schedule", columnList = "schedule_id"),
    @Index(name = "idx_departure_stop", columnList = "stop_id"),
    @Index(name = "idx_departure_time", columnList = "departureTime"),
    @Index(name = "idx_departure_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DepartureStatus status = DepartureStatus.ON_TIME;

    @Column(nullable = false)
    @Builder.Default
    private Integer delayMinutes = 0;

    @Column(length = 50)
    private String platform;

    @Column(length = 100)
    private String tripId; // External trip identifier

    @Column(columnDefinition = "TEXT")
    private String statusMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Business logic
    public boolean isDelayed() {
        return status == DepartureStatus.DELAYED && delayMinutes > 0;
    }

    public boolean isCancelled() {
        return status == DepartureStatus.CANCELLED;
    }

    public LocalDateTime getActualDepartureTime() {
        if (delayMinutes != null && delayMinutes > 0) {
            return departureTime.plusMinutes(delayMinutes);
        }
        return departureTime;
    }
}

