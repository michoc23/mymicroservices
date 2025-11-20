package com.bustransport.route.dto.request;

import jakarta.validation.constraints.NotNull;
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
    
    @NotNull(message = "Start latitude is required")
    private BigDecimal startLat;
    
    @NotNull(message = "Start longitude is required")
    private BigDecimal startLon;
    
    @NotNull(message = "End latitude is required")
    private BigDecimal endLat;
    
    @NotNull(message = "End longitude is required")
    private BigDecimal endLon;
    
    private String transportMode; // walking, transit, cycling
    private Boolean avoidTransfers;
    private Integer maxWalkDistance; // in meters
    private Boolean wheelchairAccessible;
}

