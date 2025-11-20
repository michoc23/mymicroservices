package com.bustransport.route.dto.response;

import com.bustransport.route.enums.DepartureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartureDTO {
    private Long id;
    private Long scheduleId;
    private Long stopId;
    private String stopName;
    private String routeNumber;
    private String routeName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private DepartureStatus status;
    private Integer delayMinutes;
    private String platform;
    private String tripId;
    private String statusMessage;
    private LocalDateTime actualDepartureTime;
}

