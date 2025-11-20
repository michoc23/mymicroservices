package com.bustransport.geolocation.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class TelemetryIngestDTO {
    
    @NotNull(message = "Device ID is required")
    private String deviceId;
    
    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;
    
    private BigDecimal speed;
    private BigDecimal heading;
    private BigDecimal altitude;
    private BigDecimal accuracy;
    
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
    
    private BigDecimal odometer;
}

