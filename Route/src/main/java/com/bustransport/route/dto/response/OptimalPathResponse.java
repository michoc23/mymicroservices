package com.bustransport.route.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimalPathResponse {
    private BigDecimal totalDistance; // in kilometers
    private Integer totalDuration; // in minutes
    private List<PathSegment> segments;
    private String mapPolyline;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathSegment {
        private String type; // walk, transit
        private String routeNumber;
        private String routeName;
        private String fromStopName;
        private String toStopName;
        private BigDecimal distance;
        private Integer duration;
        private String instructions;
        private List<Coordinate> path;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        private BigDecimal lat;
        private BigDecimal lon;
    }
}

