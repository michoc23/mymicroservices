package com.bustransport.route.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimalPathRequest {
    
    // Option 1: Use stop IDs (will look up coordinates)
    private Long originStopId;
    private Long destinationStopId;
    
    // Option 2: Use direct coordinates
    private BigDecimal startLat;
    private BigDecimal startLon;
    private BigDecimal endLat;
    private BigDecimal endLon;
    
    private String transportMode; // walking, transit, cycling
    private Boolean avoidTransfers;
    private Integer maxWalkDistance; // in meters
    private Boolean wheelchairAccessible;
}

