package com.bustransport.route.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "route_stops", indexes = {
    @Index(name = "idx_route_stop_route", columnList = "route_id"),
    @Index(name = "idx_route_stop_stop", columnList = "stop_id"),
    @Index(name = "idx_route_stop_sequence", columnList = "route_id, stopSequence")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"route_id", "stop_id", "stopSequence"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(nullable = false)
    private Integer stopSequence;

    @Column(precision = 10, scale = 2)
    private BigDecimal distanceFromStart; // in kilometers

    @Column
    private LocalTime timeFromStart; // Expected travel time from first stop

    @Column(nullable = false)
    @Builder.Default
    private Integer dwellTime = 30; // Stop duration in seconds

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

