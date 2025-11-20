package com.bustransport.route.dto.response;

import com.bustransport.route.enums.StopType;
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
public class StopDTO {
    private Long id;
    private String stopCode;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private StopType stopType;
    private Boolean isActive;
    private Boolean hasWheelchairAccess;
    private Boolean hasShelter;
    private Boolean hasRealTimeInfo;
    private String zone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

