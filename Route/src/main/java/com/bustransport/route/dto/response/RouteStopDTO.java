package com.bustransport.route.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopDTO {
    private Long id;
    private Long routeId;
    private StopDTO stop;
    private Integer stopSequence;
    private BigDecimal distanceFromStart;
    private LocalTime timeFromStart;
    private Integer dwellTime;
}

