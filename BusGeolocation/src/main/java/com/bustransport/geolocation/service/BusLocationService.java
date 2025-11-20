package com.bustransport.geolocation.service;

import com.bustransport.geolocation.dto.request.TelemetryIngestDTO;
import com.bustransport.geolocation.dto.response.BusLocationDTO;
import com.bustransport.geolocation.entity.Bus;
import com.bustransport.geolocation.entity.BusLocation;
import com.bustransport.geolocation.repository.BusLocationRepository;
import com.bustransport.geolocation.repository.BusRepository;
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
public class BusLocationService {

    private final BusLocationRepository locationRepository;
    private final BusRepository busRepository;

    @Transactional
    public BusLocationDTO ingestTelemetry(TelemetryIngestDTO telemetry) {
        Bus bus = busRepository.findByDeviceId(telemetry.getDeviceId())
            .orElseThrow(() -> new RuntimeException("Bus not found for device: " + telemetry.getDeviceId()));

        BusLocation location = BusLocation.builder()
            .bus(bus)
            .latitude(telemetry.getLatitude())
            .longitude(telemetry.getLongitude())
            .speed(telemetry.getSpeed())
            .heading(telemetry.getHeading())
            .altitude(telemetry.getAltitude())
            .accuracy(telemetry.getAccuracy())
            .recordedAt(telemetry.getTimestamp())
            .odometer(telemetry.getOdometer())
            .build();

        BusLocation saved = locationRepository.save(location);
        return toDTO(saved);
    }

    public BusLocationDTO getCurrentLocation(Long busId) {
        BusLocation location = locationRepository.findLatestByBusId(busId)
            .orElseThrow(() -> new RuntimeException("No location found for bus: " + busId));
        return toDTO(location);
    }

    public List<BusLocationDTO> getLocationHistory(Long busId, LocalDateTime startTime, LocalDateTime endTime) {
        return locationRepository.findByBusIdAndRecordedAtBetween(busId, startTime, endTime)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<BusLocationDTO> getAllActiveBusLocations() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        return locationRepository.findAllLatestLocations(since)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private BusLocationDTO toDTO(BusLocation location) {
        return BusLocationDTO.builder()
            .id(location.getId())
            .busId(location.getBus().getId())
            .busNumber(location.getBus().getBusNumber())
            .routeId(location.getBus().getRouteId())
            .latitude(location.getLatitude())
            .longitude(location.getLongitude())
            .speed(location.getSpeed())
            .heading(location.getHeading())
            .altitude(location.getAltitude())
            .accuracy(location.getAccuracy())
            .recordedAt(location.getRecordedAt())
            .odometer(location.getOdometer())
            .build();
    }
}

