package com.bustransport.route.entity;

import com.bustransport.route.enums.RouteType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes", indexes = {
    @Index(name = "idx_route_number", columnList = "routeNumber"),
    @Index(name = "idx_route_type", columnList = "routeType"),
    @Index(name = "idx_route_active", columnList = "isActive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String routeNumber;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteType routeType;

    @Column(length = 100)
    private String operatorId;

    @Column(nullable = false)
    private Long startStopId;

    @Column(nullable = false)
    private Long endStopId;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalDistance; // in kilometers

    @Column
    private Integer estimatedDuration; // in minutes

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(length = 20)
    private String color; // Hex color for map display

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String polyline; // Encoded polyline for route geometry

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteStop> routeStops = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void addRouteStop(RouteStop routeStop) {
        routeStops.add(routeStop);
        routeStop.setRoute(this);
    }

    public void removeRouteStop(RouteStop routeStop) {
        routeStops.remove(routeStop);
        routeStop.setRoute(null);
    }
}

