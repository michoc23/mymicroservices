package com.bustransport.geolocation.simulation;

import com.bustransport.geolocation.entity.Bus;
import com.bustransport.geolocation.entity.BusLocation;
import com.bustransport.geolocation.entity.GeofenceAlert;
import com.bustransport.geolocation.enums.AlertSeverity;
import com.bustransport.geolocation.enums.AlertType;
import com.bustransport.geolocation.enums.BusStatus;
import com.bustransport.geolocation.repository.BusLocationRepository;
import com.bustransport.geolocation.repository.BusRepository;
import com.bustransport.geolocation.repository.GeofenceAlertRepository;
import com.bustransport.geolocation.websocket.LocationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusSimulator {

    private final BusRepository busRepository;
    private final BusLocationRepository locationRepository;
    private final GeofenceAlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${simulation.enabled:true}")
    private boolean simulationEnabled;

    @Value("${simulation.speed-variation:0.2}")
    private double speedVariation;

    @Value("${simulation.maintenance-probability:0.02}")
    private double maintenanceProbability;

    private final Random random = new Random();

    // Paris city center bounds for simulation
    private static final double PARIS_LAT_MIN = 48.815;
    private static final double PARIS_LAT_MAX = 48.902;
    private static final double PARIS_LON_MIN = 2.225;
    private static final double PARIS_LON_MAX = 2.470;

    @Scheduled(fixedDelayString = "${simulation.update-interval-seconds:10}000")
    public void simulateBusMovements() {
        if (!simulationEnabled) {
            return;
        }

        try {
            List<Bus> activeBuses = busRepository.findByStatus(BusStatus.ACTIVE);
            log.debug("Simulating movement for {} active buses", activeBuses.size());

            for (Bus bus : activeBuses) {
                simulateBusMovement(bus);
            }
        } catch (Exception e) {
            log.error("Error during bus simulation", e);
        }
    }

    private void simulateBusMovement(Bus bus) {
        try {
            // Get last known location or create initial one
            BusLocation lastLocation = locationRepository
                .findLatestByBusId(bus.getId())
                .orElse(createInitialLocation(bus));

            // Calculate new position
            BusLocation newLocation = calculateNextPosition(bus, lastLocation);

            // Save location
            locationRepository.save(newLocation);

            // Check for alerts
            checkAndGenerateAlerts(bus, newLocation);

            // Broadcast via WebSocket
            broadcastLocation(newLocation);

            log.debug("Updated location for bus {}: ({}, {})",
                bus.getBusNumber(), newLocation.getLatitude(), newLocation.getLongitude());

        } catch (Exception e) {
            log.error("Error simulating movement for bus {}", bus.getBusNumber(), e);
        }
    }

    private BusLocation createInitialLocation(Bus bus) {
        // Start at a random position in Paris
        return BusLocation.builder()
            .bus(bus)
            .latitude(randomLatitude())
            .longitude(randomLongitude())
            .speed(BigDecimal.ZERO)
            .heading(randomHeading())
            .accuracy(new BigDecimal("10.0"))
            .recordedAt(LocalDateTime.now().minusSeconds(10))
            .odometer(BigDecimal.ZERO)
            .build();
    }

    private BusLocation calculateNextPosition(Bus bus, BusLocation lastLocation) {
        LocalDateTime now = LocalDateTime.now();
        
        // Base speed: 20-40 km/h with variations
        double baseSpeed = 30.0;
        double speedVar = (random.nextDouble() - 0.5) * 2 * speedVariation * baseSpeed;
        BigDecimal speed = new BigDecimal(baseSpeed + speedVar).setScale(2, RoundingMode.HALF_UP);

        // Calculate time elapsed in hours
        double hoursElapsed = 10.0 / 3600.0; // 10 seconds

        // Calculate distance traveled (km)
        double distanceKm = speed.doubleValue() * hoursElapsed;

        // Update heading with slight variations
        BigDecimal newHeading = lastLocation.getHeading()
            .add(new BigDecimal((random.nextDouble() - 0.5) * 30))
            .remainder(new BigDecimal("360"));
        if (newHeading.compareTo(BigDecimal.ZERO) < 0) {
            newHeading = newHeading.add(new BigDecimal("360"));
        }

        // Convert heading to radians
        double headingRad = Math.toRadians(newHeading.doubleValue());

        // Calculate new position using simple linear movement
        // (In reality, would follow route geometry)
        double latChange = distanceKm * Math.cos(headingRad) / 111.0; // 1 degree lat ≈ 111 km
        double lonChange = distanceKm * Math.sin(headingRad) / (111.0 * Math.cos(Math.toRadians(lastLocation.getLatitude().doubleValue())));

        BigDecimal newLat = lastLocation.getLatitude().add(new BigDecimal(latChange)).setScale(7, RoundingMode.HALF_UP);
        BigDecimal newLon = lastLocation.getLongitude().add(new BigDecimal(lonChange)).setScale(7, RoundingMode.HALF_UP);

        // Keep within Paris bounds
        newLat = constrainValue(newLat, PARIS_LAT_MIN, PARIS_LAT_MAX);
        newLon = constrainValue(newLon, PARIS_LON_MIN, PARIS_LON_MAX);

        // Update odometer
        BigDecimal newOdometer = (lastLocation.getOdometer() != null ? lastLocation.getOdometer() : BigDecimal.ZERO)
            .add(new BigDecimal(distanceKm)).setScale(2, RoundingMode.HALF_UP);

        return BusLocation.builder()
            .bus(bus)
            .latitude(newLat)
            .longitude(newLon)
            .speed(speed)
            .heading(newHeading)
            .altitude(new BigDecimal("50.0")) // Paris elevation ~50m
            .accuracy(new BigDecimal(5 + random.nextInt(10))) // 5-15m accuracy
            .recordedAt(now)
            .odometer(newOdometer)
            .build();
    }

    private void checkAndGenerateAlerts(Bus bus, BusLocation location) {
        // Random maintenance alerts (2% probability)
        if (random.nextDouble() < maintenanceProbability) {
            generateAlert(bus, location, AlertType.MAINTENANCE_REQUIRED, AlertSeverity.MEDIUM,
                "Scheduled maintenance due for bus " + bus.getBusNumber());
        }

        // Check if stopped too long (speed < 1 km/h for simulation purposes, trigger alert)
        if (location.getSpeed().compareTo(new BigDecimal("1")) < 0 && random.nextDouble() < 0.05) {
            generateAlert(bus, location, AlertType.STOPPED_TOO_LONG, AlertSeverity.LOW,
                "Bus stopped for extended period");
        }

        // Random emergency alerts (very rare, 0.1% probability)
        if (random.nextDouble() < 0.001) {
            generateAlert(bus, location, AlertType.EMERGENCY, AlertSeverity.CRITICAL,
                "Emergency situation reported");
        }

        // Off-route detection (5% chance for simulation)
        if (random.nextDouble() < 0.05) {
            generateAlert(bus, location, AlertType.OFF_ROUTE, AlertSeverity.HIGH,
                "Bus detected off designated route");
        }
    }

    private void generateAlert(Bus bus, BusLocation location, AlertType alertType, 
                               AlertSeverity severity, String description) {
        // Check if similar alert already exists
        List<GeofenceAlert> existingAlerts = alertRepository.findByBusIdAndAcknowledgedAtIsNull(bus.getId());
        boolean alreadyHasAlert = existingAlerts.stream()
            .anyMatch(alert -> alert.getAlertType() == alertType);

        if (!alreadyHasAlert) {
            GeofenceAlert alert = GeofenceAlert.builder()
                .bus(bus)
                .alertType(alertType)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .description(description)
                .severity(severity)
                .triggeredAt(LocalDateTime.now())
                .build();

            alertRepository.save(alert);
            log.warn("Alert generated for bus {}: {} - {}", bus.getBusNumber(), alertType, description);

            // Would publish event here for notification service
            // eventPublisher.publishAlert(alert);
        }
    }

    private void broadcastLocation(BusLocation location) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/bus-locations",
                location
            );
            messagingTemplate.convertAndSend(
                "/topic/bus/" + location.getBus().getId(),
                location
            );
        } catch (Exception e) {
            log.error("Error broadcasting location", e);
        }
    }

    private BigDecimal randomLatitude() {
        double lat = PARIS_LAT_MIN + (PARIS_LAT_MAX - PARIS_LAT_MIN) * random.nextDouble();
        return new BigDecimal(lat).setScale(7, RoundingMode.HALF_UP);
    }

    private BigDecimal randomLongitude() {
        double lon = PARIS_LON_MIN + (PARIS_LON_MAX - PARIS_LON_MIN) * random.nextDouble();
        return new BigDecimal(lon).setScale(7, RoundingMode.HALF_UP);
    }

    private BigDecimal randomHeading() {
        return new BigDecimal(random.nextInt(360)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal constrainValue(BigDecimal value, double min, double max) {
        if (value.doubleValue() < min) {
            return new BigDecimal(min).setScale(value.scale(), RoundingMode.HALF_UP);
        }
        if (value.doubleValue() > max) {
            return new BigDecimal(max).setScale(value.scale(), RoundingMode.HALF_UP);
        }
        return value;
    }
}

