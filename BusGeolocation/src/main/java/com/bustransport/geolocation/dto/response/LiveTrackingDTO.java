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
public class LiveTrackingDTO {
    private Long busId;
    private String busNumber;
    private Long routeId;
    private String routeName;
    private BusLocationDTO currentLocation;
    private String nextStopName;
    private Integer nextStopETA; // minutes
    private Boolean onSchedule;
    private Integer passengersOnBoard;
    private LocalDateTime lastUpdate;
}

