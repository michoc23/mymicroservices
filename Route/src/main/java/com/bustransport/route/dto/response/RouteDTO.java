package com.bustransport.route.dto.response;

import com.bustransport.route.enums.RouteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    private Long id;
    private String routeNumber;
    private String name;
    private RouteType routeType;
    private String operatorId;
    private Long startStopId;
    private Long endStopId;
    private BigDecimal totalDistance;
    private Integer estimatedDuration;
    private Boolean isActive;
    private String color;
    private String description;
    private String polyline;
    private List<RouteStopDTO> stops;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

