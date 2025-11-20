package com.bustransport.geolocation.service;

import com.bustransport.geolocation.dto.response.AlertDTO;
import com.bustransport.geolocation.entity.GeofenceAlert;
import com.bustransport.geolocation.repository.GeofenceAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AlertService {

    private final GeofenceAlertRepository alertRepository;

    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findActiveAlerts()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<AlertDTO> getAlertsByBus(Long busId) {
        return alertRepository.findByBusIdAndAcknowledgedAtIsNull(busId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<AlertDTO> getRecentAlerts(int hours) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(hours);
        return alertRepository.findRecentAlerts(fromTime)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public AlertDTO acknowledgeAlert(Long alertId, String acknowledgedBy, String resolution) {
        GeofenceAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        alert.acknowledge(acknowledgedBy, resolution);
        GeofenceAlert saved = alertRepository.save(alert);
        
        return toDTO(saved);
    }

    private AlertDTO toDTO(GeofenceAlert alert) {
        return AlertDTO.builder()
            .id(alert.getId())
            .busId(alert.getBus().getId())
            .busNumber(alert.getBus().getBusNumber())
            .alertType(alert.getAlertType())
            .latitude(alert.getLatitude())
            .longitude(alert.getLongitude())
            .description(alert.getDescription())
            .severity(alert.getSeverity())
            .triggeredAt(alert.getTriggeredAt())
            .acknowledgedAt(alert.getAcknowledgedAt())
            .acknowledgedBy(alert.getAcknowledgedBy())
            .resolution(alert.getResolution())
            .isActive(alert.isActive())
            .build();
    }
}

