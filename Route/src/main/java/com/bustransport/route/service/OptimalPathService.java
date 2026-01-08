package com.bustransport.route.service;

import com.bustransport.route.dto.request.OptimalPathRequest;
import com.bustransport.route.dto.response.OptimalPathResponse;
import com.bustransport.route.dto.response.OptimalPathResponse.PathSegment;
import com.bustransport.route.dto.response.OptimalPathResponse.Coordinate;
import com.bustransport.route.entity.Route;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OptimalPathService {

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final RoutingService routingService;

    @Cacheable(value = "optimalPaths")
    public OptimalPathResponse calculateOptimalPath(OptimalPathRequest request) {
        List<Stop> startStops;
        List<Stop> endStops;

        if (request.getOriginStopId() != null && request.getDestinationStopId() != null) {
            startStops = List.of(stopRepository.findById(request.getOriginStopId())
                    .orElseThrow(() -> new IllegalArgumentException("Origin stop not found")));
            endStops = List.of(stopRepository.findById(request.getDestinationStopId())
                    .orElseThrow(() -> new IllegalArgumentException("Destination stop not found")));

            request.setStartLat(startStops.get(0).getLatitude());
            request.setStartLon(startStops.get(0).getLongitude());
            request.setEndLat(endStops.get(0).getLatitude());
            request.setEndLon(endStops.get(0).getLongitude());
        } else {
            double searchRadius = 1.0;
            startStops = stopRepository.findNearbyStops(request.getStartLat(), request.getStartLon(), searchRadius);
            endStops = stopRepository.findNearbyStops(request.getEndLat(), request.getEndLon(), searchRadius);

            if (startStops.isEmpty() || endStops.isEmpty()) {
                return buildWalkingOnlyPath(request);
            }
        }

        // Try direct route first
        for (Stop s1 : startStops) {
            for (Stop s2 : endStops) {
                List<Route> commonRoutes = findCommonRoutes(s1.getId(), s2.getId());
                if (!commonRoutes.isEmpty()) {
                    // Pick the first available direct route
                    return buildTransitPath(request, s1, s2, commonRoutes.get(0));
                }
            }
        }

        // Try routes with 1 transfer
        for (Stop s1 : startStops) {
            List<Route> routesFromStart = routeRepository.findByStopId(s1.getId());
            for (Route r1 : routesFromStart) {
                List<Stop> stopsOnR1 = stopRepository.findByRouteId(r1.getId());
                int startIdx = findStopIndex(stopsOnR1, s1.getId());
                if (startIdx == -1)
                    continue;

                for (int i = startIdx + 1; i < stopsOnR1.size(); i++) {
                    Stop transferStop = stopsOnR1.get(i);
                    for (Stop s2 : endStops) {
                        List<Route> commonRoutes = findCommonRoutes(transferStop.getId(), s2.getId());
                        // Ensure we don't take the same route again
                        Route r2 = commonRoutes.stream()
                                .filter(r -> !r.getId().equals(r1.getId()))
                                .findFirst().orElse(null);

                        if (r2 != null) {
                            return buildMultiLegTransitPath(request, s1, transferStop, s2, r1, r2);
                        }
                    }
                }
            }
        }

        return buildWalkingOnlyPath(request);
    }

    private List<Route> findCommonRoutes(Long stopId1, Long stopId2) {
        List<Route> routes1 = routeRepository.findByStopId(stopId1);
        List<Route> routes2 = routeRepository.findByStopId(stopId2);
        List<Route> common = new ArrayList<>();

        for (Route r1 : routes1) {
            for (Route r2 : routes2) {
                if (r1.getId().equals(r2.getId())) {
                    // Check direction: stopId1 must come before stopId2
                    List<Stop> stopsOnRoute = stopRepository.findByRouteId(r1.getId());
                    int idx1 = findStopIndex(stopsOnRoute, stopId1);
                    int idx2 = findStopIndex(stopsOnRoute, stopId2);
                    if (idx1 != -1 && idx2 != -1 && idx1 < idx2) {
                        common.add(r1);
                    }
                }
            }
        }
        return common;
    }

    private int findStopIndex(List<Stop> stops, Long stopId) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getId().equals(stopId))
                return i;
        }
        return -1;
    }

    private OptimalPathResponse buildMultiLegTransitPath(
            OptimalPathRequest request,
            Stop s1, Stop transferStop, Stop s2,
            Route r1, Route r2) {

        List<PathSegment> segments = new ArrayList<>();

        // 1. Walk to s1
        segments.add(createWalkSegment(request.getStartLat(), request.getStartLon(), s1, "Walk to " + s1.getName()));

        // 2. Transit r1 to transferStop
        segments.add(createTransitSegment(s1, transferStop, r1));

        // 3. Transfer/Transit r2 to s2
        segments.add(createTransitSegment(transferStop, s2, r2));

        // 4. Walk to destination
        segments.add(createWalkSegment(s2.getLatitude(), s2.getLongitude(),
                Stop.builder().name("Destination").latitude(request.getEndLat()).longitude(request.getEndLon()).build(),
                "Walk to destination"));

        return finalizeResponse(segments);
    }

    private PathSegment createWalkSegment(BigDecimal lat1, BigDecimal lon1, Stop target, String instruction) {
        BigDecimal dist = calculateDistance(lat1, lon1, target.getLatitude(), target.getLongitude());
        return PathSegment.builder()
                .type("walk")
                .distance(dist)
                .duration((int) (dist.doubleValue() * 12))
                .instructions(instruction)
                .path(routingService.getWalkingRouteGeometry(lat1, lon1, target.getLatitude(), target.getLongitude()))
                .build();
    }

    private PathSegment createTransitSegment(Stop start, Stop end, Route route) {
        BigDecimal dist = calculateDistance(start.getLatitude(), start.getLongitude(), end.getLatitude(),
                end.getLongitude());

        // Get intermediate stops
        List<Stop> allStops = stopRepository.findByRouteId(route.getId());
        int startIdx = findStopIndex(allStops, start.getId());
        int endIdx = findStopIndex(allStops, end.getId());
        List<String> intermediate = new ArrayList<>();
        if (startIdx != -1 && endIdx != -1) {
            for (int i = startIdx + 1; i < endIdx; i++) {
                intermediate.add(allStops.get(i).getName());
            }
        }

        // Use polyline if available, otherwise OSRM
        List<Coordinate> path;
        if (route.getPolyline() != null && !route.getPolyline().isEmpty()) {
            path = decodePolyline(route.getPolyline());
            // In a real scenario, we might want to slice the polyline to only include the
            // portion between the two stops
        } else {
            path = routingService.getRouteGeometry(start.getLatitude(), start.getLongitude(), end.getLatitude(),
                    end.getLongitude());
        }

        return PathSegment.builder()
                .type("transit")
                .routeNumber(route.getRouteNumber())
                .routeName(route.getName())
                .routeColor(route.getColor())
                .fromStopName(start.getName())
                .toStopName(end.getName())
                .intermediateStops(intermediate)
                .distance(dist)
                .duration((int) (dist.doubleValue() * 2.5))
                .instructions("Take " + route.getRouteNumber() + " from " + start.getName() + " to " + end.getName())
                .path(path)
                .build();
    }

    private OptimalPathResponse finalizeResponse(List<PathSegment> segments) {
        BigDecimal totalDistance = segments.stream().map(PathSegment::getDistance).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        Integer totalDuration = segments.stream().mapToInt(PathSegment::getDuration).sum();

        return OptimalPathResponse.builder()
                .totalDistance(totalDistance)
                .totalDuration(totalDuration)
                .segments(segments)
                .mapPolyline("") // We'll let the frontend join the segments
                .build();
    }

    private List<Coordinate> decodePolyline(String polyline) {
        // Mocking polyline decoding for now, in a real app use a library like
        // google-maps-services-java
        List<Coordinate> coords = new ArrayList<>();
        if (polyline.contains(";")) {
            String[] points = polyline.split(";");
            for (String p : points) {
                String[] latLon = p.split(",");
                if (latLon.length == 2) {
                    coords.add(new Coordinate(new BigDecimal(latLon[0]), new BigDecimal(latLon[1])));
                }
            }
        }
        return coords;
    }

    private OptimalPathResponse buildTransitPath(
            OptimalPathRequest request,
            Stop startStop,
            Stop endStop,
            Route route) {

        List<PathSegment> segments = new ArrayList<>();
        segments.add(createWalkSegment(request.getStartLat(), request.getStartLon(), startStop,
                "Walk to " + startStop.getName()));
        segments.add(createTransitSegment(startStop, endStop, route));
        segments.add(createWalkSegment(endStop.getLatitude(), endStop.getLongitude(),
                Stop.builder().name("Destination").latitude(request.getEndLat()).longitude(request.getEndLon()).build(),
                "Walk to destination"));

        return finalizeResponse(segments);
    }

    private OptimalPathResponse buildWalkingOnlyPath(OptimalPathRequest request) {
        BigDecimal distance = calculateDistance(
                request.getStartLat(), request.getStartLon(),
                request.getEndLat(), request.getEndLon());

        // Get real walking route geometry
        List<Coordinate> walkingPath = routingService.getWalkingRouteGeometry(
                request.getStartLat(), request.getStartLon(),
                request.getEndLat(), request.getEndLon());

        PathSegment walkSegment = PathSegment.builder()
                .type("walk")
                .distance(distance)
                .duration((int) (distance.doubleValue() * 12)) // ~5 km/h walking speed
                .instructions("Walk to destination")
                .path(walkingPath)
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
        // Simple polyline encoding - in production would use actual polyline encoding
        // algorithm
        StringBuilder polyline = new StringBuilder();
        for (PathSegment segment : segments) {
            for (Coordinate coord : segment.getPath()) {
                polyline.append(coord.getLat()).append(",")
                        .append(coord.getLon()).append(";");
            }
        }
        return polyline.toString();
    }

}
