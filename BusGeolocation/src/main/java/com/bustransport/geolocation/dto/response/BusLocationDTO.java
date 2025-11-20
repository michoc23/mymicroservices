package com.bustransport.geolocation.dto.response;

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
public class BusLocationDTO {
    private Long id;
    private Long busId;
    private String busNumber;
    private Long routeId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speed;
    private BigDecimal heading;
    private BigDecimal altitude;
    private BigDecimal accuracy;
    private LocalDateTime recordedAt;
    private BigDecimal odometer;
}

