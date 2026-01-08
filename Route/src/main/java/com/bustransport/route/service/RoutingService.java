package com.bustransport.route.service;

import com.bustransport.route.dto.response.OptimalPathResponse.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for routing using OSRM (Open Source Routing Machine)
 * Provides real-world road network routing instead of straight lines
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final RestTemplate restTemplate;

    @Value("${routing.osrm.url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    /**
     * Get route geometry between two points using OSRM driving profile
     * @param lat1 Start latitude
     * @param lon1 Start longitude
     * @param lat2 End latitude
     * @param lon2 End longitude
     * @return List of coordinates following real roads
     */
    @Cacheable(value = "routeGeometry", key = "#lat1 + '_' + #lon1 + '_' + #lat2 + '_' + #lon2")
    public List<Coordinate> getRouteGeometry(BigDecimal lat1, BigDecimal lon1, 
                                            BigDecimal lat2, BigDecimal lon2) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(osrmBaseUrl + "/route/v1/driving/{lon1},{lat1};{lon2},{lat2}")
                    .queryParam("overview", "full")
                    .queryParam("geometries", "geojson")
                    .buildAndExpand(lon1, lat1, lon2, lat2)
                    .toUriString();

            log.debug("Requesting route from OSRM: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
                
                if (routes != null && !routes.isEmpty()) {
                    Map<String, Object> route = routes.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) route.get("geometry");
                    
                    if (geometry != null) {
                        List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
                        return convertToCoordinates(coordinates);
                    }
                }
            }
            
            log.warn("OSRM returned empty route, falling back to straight line");
            return getStraightLineFallback(lat1, lon1, lat2, lon2);
            
        } catch (Exception e) {
            log.error("Error fetching route from OSRM: {}", e.getMessage(), e);
            // Fallback to straight line if OSRM fails
            return getStraightLineFallback(lat1, lon1, lat2, lon2);
        }
    }

    /**
     * Get route geometry for walking between two points
     */
    @Cacheable(value = "walkRouteGeometry", key = "#lat1 + '_' + #lon1 + '_' + #lat2 + '_' + #lon2")
    public List<Coordinate> getWalkingRouteGeometry(BigDecimal lat1, BigDecimal lon1, 
                                                   BigDecimal lat2, BigDecimal lon2) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(osrmBaseUrl + "/route/v1/foot/{lon1},{lat1};{lon2},{lat2}")
                    .queryParam("overview", "full")
                    .queryParam("geometries", "geojson")
                    .buildAndExpand(lon1, lat1, lon2, lat2)
                    .toUriString();

            log.debug("Requesting walking route from OSRM: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
                
                if (routes != null && !routes.isEmpty()) {
                    Map<String, Object> route = routes.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) route.get("geometry");
                    
                    if (geometry != null) {
                        List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
                        return convertToCoordinates(coordinates);
                    }
                }
            }
            
            log.warn("OSRM returned empty walking route, falling back to straight line");
            return getStraightLineFallback(lat1, lon1, lat2, lon2);
            
        } catch (Exception e) {
            log.error("Error fetching walking route from OSRM: {}", e.getMessage(), e);
            return getStraightLineFallback(lat1, lon1, lat2, lon2);
        }
    }

    /**
     * Convert OSRM GeoJSON coordinates to our Coordinate format
     * OSRM returns [longitude, latitude] pairs
     */
    private List<Coordinate> convertToCoordinates(List<List<Double>> osrmCoords) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (List<Double> coord : osrmCoords) {
            if (coord.size() >= 2) {
                coordinates.add(new Coordinate(
                    BigDecimal.valueOf(coord.get(1)), // latitude
                    BigDecimal.valueOf(coord.get(0))  // longitude
                ));
            }
        }
        return coordinates;
    }

    /**
     * Fallback to straight line if routing service fails
     */
    private List<Coordinate> getStraightLineFallback(BigDecimal lat1, BigDecimal lon1, 
                                                    BigDecimal lat2, BigDecimal lon2) {
        List<Coordinate> fallback = new ArrayList<>();
        fallback.add(new Coordinate(lat1, lon1));
        fallback.add(new Coordinate(lat2, lon2));
        return fallback;
    }
}
