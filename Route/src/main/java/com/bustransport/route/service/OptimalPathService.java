package com.bustransport.route.service;

import com.bustransport.route.dto.request.OptimalPathRequest;
import com.bustransport.route.dto.response.OptimalPathResponse;
import com.bustransport.route.dto.response.OptimalPathResponse.PathSegment;
import com.bustransport.route.dto.response.OptimalPathResponse.Coordinate;
import com.bustransport.route.entity.Route;
import com.bustransport.route.entity.RouteStop;
import com.bustransport.route.entity.Stop;
import com.bustransport.route.repository.RouteRepository;
import com.bustransport.route.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OptimalPathService {

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;

    @Cacheable(value = "optimalPaths", key = "#request.startLat + '_' + #request.startLon + '_' + #request.endLat + '_' + #request.endLon")
    public OptimalPathResponse calculateOptimalPath(OptimalPathRequest request) {
        log.debug("Calculating optimal path from ({}, {}) to ({}, {})", 
            request.getStartLat(), request.getStartLon(), 
            request.getEndLat(), request.getEndLon());

        // Find nearest stops to start and end points
        double searchRadius = 1.0; // 1 km radius
        List<Stop> startStops = stopRepository.findNearbyStops(
            request.getStartLat(), request.getStartLon(), searchRadius);
        List<Stop> endStops = stopRepository.findNearbyStops(
            request.getEndLat(), request.getEndLon(), searchRadius);

        if (startStops.isEmpty() || endStops.isEmpty()) {
            return buildWalkingOnlyPath(request);
        }

        // Find the best route connection
        Stop nearestStartStop = startStops.get(0);
        Stop nearestEndStop = endStops.get(0);

        // Check if there's a direct route between these stops
        List<Route> routesFromStart = routeRepository.findByStopId(nearestStartStop.getId());
        List<Route> routesToEnd = routeRepository.findByStopId(nearestEndStop.getId());

        Route directRoute = findCommonRoute(routesFromStart, routesToEnd);

        if (directRoute != null) {
            return buildTransitPath(request, nearestStartStop, nearestEndStop, directRoute);
        }

        // No direct route found, suggest walking or multi-leg journey
        return buildWalkingOnlyPath(request);
    }

    private Route findCommonRoute(List<Route> routes1, List<Route> routes2) {
        for (Route r1 : routes1) {
            for (Route r2 : routes2) {
                if (r1.getId().equals(r2.getId())) {
                    return r1;
                }
            }
        }
        return null;
    }

    private OptimalPathResponse buildTransitPath(
            OptimalPathRequest request, 
            Stop startStop, 
            Stop endStop, 
            Route route) {
        
        List<PathSegment> segments = new ArrayList<>();

        // Walking segment to start stop
        BigDecimal walkToStopDistance = calculateDistance(
            request.getStartLat(), request.getStartLon(),
            startStop.getLatitude(), startStop.getLongitude()
        );
        
        segments.add(PathSegment.builder()
            .type("walk")
            .distance(walkToStopDistance)
            .duration((int)(walkToStopDistance.doubleValue() * 12)) // ~5 km/h walking speed
            .instructions("Walk to " + startStop.getName())
            .path(List.of(
                new Coordinate(request.getStartLat(), request.getStartLon()),
                new Coordinate(startStop.getLatitude(), startStop.getLongitude())
            ))
            .build());

        // Transit segment
        BigDecimal transitDistance = calculateDistance(
            startStop.getLatitude(), startStop.getLongitude(),
            endStop.getLatitude(), endStop.getLongitude()
        );
        
        segments.add(PathSegment.builder()
            .type("transit")
            .routeNumber(route.getRouteNumber())
            .routeName(route.getName())
            .fromStopName(startStop.getName())
            .toStopName(endStop.getName())
            .distance(transitDistance)
            .duration(route.getEstimatedDuration() != null ? route.getEstimatedDuration() : 
                     (int)(transitDistance.doubleValue() * 2.5)) // ~24 km/h average bus speed
            .instructions("Take " + route.getRouteNumber() + " from " + startStop.getName() + " to " + endStop.getName())
            .path(List.of(
                new Coordinate(startStop.getLatitude(), startStop.getLongitude()),
                new Coordinate(endStop.getLatitude(), endStop.getLongitude())
            ))
            .build());

        // Walking segment from end stop
        BigDecimal walkFromStopDistance = calculateDistance(
            endStop.getLatitude(), endStop.getLongitude(),
            request.getEndLat(), request.getEndLon()
        );
        
        segments.add(PathSegment.builder()
            .type("walk")
            .distance(walkFromStopDistance)
            .duration((int)(walkFromStopDistance.doubleValue() * 12))
            .instructions("Walk to destination")
            .path(List.of(
                new Coordinate(endStop.getLatitude(), endStop.getLongitude()),
                new Coordinate(request.getEndLat(), request.getEndLon())
            ))
            .build());

        BigDecimal totalDistance = walkToStopDistance.add(transitDistance).add(walkFromStopDistance);
        Integer totalDuration = segments.stream()
            .mapToInt(PathSegment::getDuration)
            .sum();

        return OptimalPathResponse.builder()
            .totalDistance(totalDistance)
            .totalDuration(totalDuration)
            .segments(segments)
            .mapPolyline(encodePolyline(segments))
            .build();
    }

    private OptimalPathResponse buildWalkingOnlyPath(OptimalPathRequest request) {
        BigDecimal distance = calculateDistance(
            request.getStartLat(), request.getStartLon(),
            request.getEndLat(), request.getEndLon()
        );

        PathSegment walkSegment = PathSegment.builder()
            .type("walk")
            .distance(distance)
            .duration((int)(distance.doubleValue() * 12)) // ~5 km/h walking speed
            .instructions("Walk to destination")
            .path(List.of(
                new Coordinate(request.getStartLat(), request.getStartLon()),
                new Coordinate(request.getEndLat(), request.getEndLon())
            ))
            .build();

        return OptimalPathResponse.builder()
            .totalDistance(distance)
            .totalDuration(walkSegment.getDuration())
            .segments(List.of(walkSegment))
            .mapPolyline(encodePolyline(List.of(walkSegment)))
            .build();
    }

    private BigDecimal calculateDistance(
            BigDecimal lat1, BigDecimal lon1, 
            BigDecimal lat2, BigDecimal lon2) {
        // Haversine formula
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) * 
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private String encodePolyline(List<PathSegment> segments) {
        // Simple polyline encoding - in production would use actual polyline encoding algorithm
        StringBuilder polyline = new StringBuilder();
        for (PathSegment segment : segments) {
            for (Coordinate coord : segment.getPath()) {
                polyline.append(coord.getLat()).append(",")
                       .append(coord.getLon()).append(";");
            }
        }
        return polyline.toString();
    }

    // TODO: Integrate GraphHopper for real-world routing on OSM data
    // This would provide:
    // - Multi-modal routing (walk + transit + transfers)
    // - Real road network paths instead of straight lines
    // - Elevation data and route optimization
    // - Turn-by-turn directions
}

