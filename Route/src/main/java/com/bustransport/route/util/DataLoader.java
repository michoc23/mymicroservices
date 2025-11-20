package com.bustransport.route.util;

import com.bustransport.route.entity.*;
import com.bustransport.route.enums.*;
import com.bustransport.route.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local", "default"})
public class DataLoader implements CommandLineRunner {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (routeRepository.count() > 0) {
            log.info("Data already loaded, skipping initialization");
            return;
        }

        log.info("Loading sample Paris transit data...");
        loadSampleData();
        log.info("Sample data loaded successfully");
    }

    private void loadSampleData() {
        // Create sample stops (Paris metro and bus stops)
        Stop chatelet = createStop("CHAT", "Chatelet", 48.8583, 2.3472, StopType.TERMINAL);
        Stop gareNord = createStop("GARN", "Gare du Nord", 48.8809, 2.3553, StopType.TERMINAL);
        Stop operaStop = createStop("OPER", "Opera", 48.8708, 2.3314, StopType.REGULAR);
        Stop bastille = createStop("BAST", "Bastille", 48.8532, 2.3689, StopType.REGULAR);
        Stop nation = createStop("NATI", "Nation", 48.8483, 2.3969, StopType.TERMINAL);
        Stop republique = createStop("REPU", "Republique", 48.8673, 2.3634, StopType.REGULAR);
        
        // Create Metro Line 1
        Route metro1 = createRoute("Line 1", "Metro Line 1: Chatelet - Nation", RouteType.METRO, chatelet.getId(), nation.getId());
        linkStopToRoute(metro1, chatelet, 1);
        linkStopToRoute(metro1, bastille, 2);
        linkStopToRoute(metro1, nation, 3);
        createSchedule(metro1, ServiceType.WEEKDAY, 5);
        
        // Create Bus 21
        Route bus21 = createRoute("Bus 21", "Bus 21: Gare du Nord - Opera", RouteType.BUS, gareNord.getId(), operaStop.getId());
        linkStopToRoute(bus21, gareNord, 1);
        linkStopToRoute(bus21, republique, 2);
        linkStopToRoute(bus21, operaStop, 3);
        createSchedule(bus21, ServiceType.WEEKDAY, 12);
        
        // Create RER A
        Route rerA = createRoute("RER A", "RER A: Chatelet - Nation", RouteType.TRAIN, chatelet.getId(), nation.getId());
        linkStopToRoute(rerA, chatelet, 1);
        linkStopToRoute(rerA, nation, 2);
        createSchedule(rerA, ServiceType.WEEKDAY, 3);
    }

    private Stop createStop(String code, String name, double lat, double lon, StopType type) {
        Stop stop = Stop.builder()
                .stopCode(code)
                .name(name)
                .latitude(BigDecimal.valueOf(lat))
                .longitude(BigDecimal.valueOf(lon))
                .stopType(type)
                .build();
        return stopRepository.save(stop);
    }

    private Route createRoute(String name, String description, RouteType type, Long startStopId, Long endStopId) {
        Route route = Route.builder()
                .routeNumber(name.replaceAll(" ", "-"))
                .name(name)
                .description(description)
                .routeType(type)
                .startStopId(startStopId)
                .endStopId(endStopId)
                .build();
        return routeRepository.save(route);
    }

    private void linkStopToRoute(Route route, Stop stop, int order) {
        RouteStop routeStop = RouteStop.builder()
                .route(route)
                .stop(stop)
                .stopSequence(order)
                .build();
        routeStopRepository.save(routeStop);
    }

    private void createSchedule(Route route, ServiceType serviceType, int frequencyMinutes) {
        Schedule schedule = Schedule.builder()
                .route(route)
                .serviceType(serviceType)
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(23, 0))
                .frequency(frequencyMinutes)
                .firstDeparture(LocalTime.of(6, 0))
                .lastDeparture(LocalTime.of(23, 0))
                .build();
        scheduleRepository.save(schedule);
    }
}
