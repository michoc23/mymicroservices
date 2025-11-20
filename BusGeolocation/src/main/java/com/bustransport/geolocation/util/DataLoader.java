package com.bustransport.geolocation.util;

import com.bustransport.geolocation.entity.Bus;
import com.bustransport.geolocation.enums.BusStatus;
import com.bustransport.geolocation.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class DataLoader implements CommandLineRunner {

    private final BusRepository busRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (busRepository.count() > 0) {
            log.info("Bus data already loaded, skipping initialization");
            return;
        }

        log.info("Loading sample bus data...");
        loadSampleBuses();
        log.info("Sample bus data loaded successfully");
    }

    private void loadSampleBuses() {
        // Create sample buses for the routes
        for (int i = 1; i <= 20; i++) {
            Long routeId = (i % 3) + 1L; // Distribute across 3 routes
            Bus bus = Bus.builder()
                .busNumber("BUS-" + String.format("%03d", i))
                .routeId(routeId)
                .capacity(50)
                .status(BusStatus.ACTIVE)
                .lastMaintenanceDate(LocalDate.now().minusDays(i * 3))
                .deviceId("DEVICE-" + String.format("%03d", i))
                .model("Mercedes Citaro")
                .plateNumber("75-ABC-" + String.format("%03d", i))
                .build();
            
            busRepository.save(bus);
        }
    }
}

