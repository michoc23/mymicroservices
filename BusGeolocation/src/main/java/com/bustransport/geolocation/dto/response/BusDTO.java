package com.bustransport.geolocation.dto.response;

import com.bustransport.geolocation.enums.BusStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusDTO {
    private Long id;
    private String busNumber;
    private Long routeId;
    private Integer capacity;
    private BusStatus status;
    private LocalDate lastMaintenanceDate;
    private String deviceId;
    private String model;
    private String plateNumber;
}

