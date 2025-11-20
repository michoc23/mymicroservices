package com.bustransport.geolocation.dto.response;

import com.bustransport.geolocation.enums.AlertSeverity;
import com.bustransport.geolocation.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private Long busId;
    private String busNumber;
    private AlertType alertType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private AlertSeverity severity;
    private LocalDateTime triggeredAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private String resolution;
    private Boolean isActive;
}

