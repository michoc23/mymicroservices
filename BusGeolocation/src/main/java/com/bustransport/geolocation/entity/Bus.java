package com.bustransport.geolocation.entity;

import com.bustransport.geolocation.enums.BusStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "buses", indexes = {
    @Index(name = "idx_bus_number", columnList = "busNumber"),
    @Index(name = "idx_bus_route", columnList = "routeId"),
    @Index(name = "idx_bus_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String busNumber;

    @Column(nullable = false)
    private Long routeId; // Reference to Route Service

    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 50;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BusStatus status = BusStatus.ACTIVE;

    @Column
    private LocalDate lastMaintenanceDate;

    @Column(nullable = false, unique = true, length = 100)
    private String deviceId; // GPS device identifier

    @Column(length = 50)
    private String model;

    @Column(length = 20)
    private String plateNumber;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == BusStatus.ACTIVE;
    }

    public boolean needsMaintenance() {
        if (lastMaintenanceDate == null) {
            return true;
        }
        return lastMaintenanceDate.plusMonths(3).isBefore(LocalDate.now());
    }
}

