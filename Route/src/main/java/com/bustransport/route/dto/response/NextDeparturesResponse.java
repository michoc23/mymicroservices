package com.bustransport.route.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextDeparturesResponse {
    private String stopName;
    private LocalDateTime queryTime;
    private List<DepartureDTO> departures;
    private Boolean hasRealTimeData;
}

