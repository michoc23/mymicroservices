package com.bustransport.route.entity;

import com.bustransport.route.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedules", indexes = {
    @Index(name = "idx_schedule_route", columnList = "route_id"),
    @Index(name = "idx_schedule_service_type", columnList = "serviceType"),
    @Index(name = "idx_schedule_valid_dates", columnList = "validFrom, validUntil")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceType serviceType;

    @Column(nullable = false)
    private LocalTime startTime; // Service starts (e.g., 05:00)

    @Column(nullable = false)
    private LocalTime endTime; // Service ends (e.g., 23:00)

    @Column(nullable = false)
    private Integer frequency; // Frequency in minutes

    @Column(nullable = false)
    private LocalTime firstDeparture;

    @Column(nullable = false)
    private LocalTime lastDeparture;

    @Column
    private LocalDate validFrom;

    @Column
    private LocalDate validUntil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

