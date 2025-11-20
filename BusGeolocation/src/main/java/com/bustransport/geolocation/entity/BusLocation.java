package com.bustransport.geolocation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bus_locations", indexes = {
    @Index(name = "idx_location_bus", columnList = "bus_id"),
    @Index(name = "idx_location_recorded", columnList = "recordedAt"),
    @Index(name = "idx_location_bus_time", columnList = "bus_id, recordedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal speed = BigDecimal.ZERO; // km/h

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal heading = BigDecimal.ZERO; // degrees (0-360)

    @Column(precision = 6, scale = 2)
    private BigDecimal altitude; // meters

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal accuracy = new BigDecimal("10.0"); // meters

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal odometer; // Total distance in km

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

