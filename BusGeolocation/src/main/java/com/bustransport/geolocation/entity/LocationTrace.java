package com.bustransport.geolocation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_traces", indexes = {
    @Index(name = "idx_trace_bus", columnList = "bus_id"),
    @Index(name = "idx_trace_route", columnList = "routeId"),
    @Index(name = "idx_trace_time", columnList = "startTime, endTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(nullable = false)
    private Long routeId;

    @Column(columnDefinition = "TEXT")
    private String locationsJson; // JSON array of location points

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDistance = BigDecimal.ZERO; // km

    @Column(nullable = false)
    @Builder.Default
    private Integer totalStops = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal averageSpeed; // km/h

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

