package com.bustransport.route.dto.response;

import com.bustransport.route.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long id;
    private Long routeId;
    private String routeName;
    private ServiceType serviceType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer frequency;
    private LocalTime firstDeparture;
    private LocalTime lastDeparture;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean isActive;
    private String notes;
}

