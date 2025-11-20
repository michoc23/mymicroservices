package com.bustransport.route.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextDeparturesRequest {
    private Long stopId;
    private Long routeId; // Optional - filter by route
    private Integer limit = 10;
    private Integer timeWindowMinutes = 120; // Next 2 hours by default
}

