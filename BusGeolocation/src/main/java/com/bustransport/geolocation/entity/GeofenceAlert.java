package com.bustransport.geolocation.entity;

import com.bustransport.geolocation.enums.AlertSeverity;
import com.bustransport.geolocation.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "geofence_alerts", indexes = {
    @Index(name = "idx_alert_bus", columnList = "bus_id"),
    @Index(name = "idx_alert_type", columnList = "alertType"),
    @Index(name = "idx_alert_severity", columnList = "severity"),
    @Index(name = "idx_alert_triggered", columnList = "triggeredAt"),
    @Index(name = "idx_alert_acknowledged", columnList = "acknowledgedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeofenceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType alertType;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AlertSeverity severity = AlertSeverity.MEDIUM;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column
    private LocalDateTime acknowledgedAt;

    @Column(length = 100)
    private String acknowledgedBy;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return acknowledgedAt == null;
    }

    public void acknowledge(String acknowledgedBy, String resolution) {
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = acknowledgedBy;
        this.resolution = resolution;
    }
}

