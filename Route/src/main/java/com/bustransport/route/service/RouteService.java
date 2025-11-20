package com.bustransport.route.service;

import com.bustransport.route.dto.response.RouteDTO;
import com.bustransport.route.entity.Route;
import com.bustransport.route.enums.RouteType;
import com.bustransport.route.exception.ResourceNotFoundException;
import com.bustransport.route.mapper.RouteMapper;
import com.bustransport.route.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;

    @Cacheable(value = "routes", key = "#id")
    public RouteDTO getRouteById(Long id) {
        log.debug("Fetching route with id: {}", id);
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return routeMapper.toDTO(route);
    }

    @Cacheable(value = "routes", key = "#routeNumber")
    public RouteDTO getRouteByNumber(String routeNumber) {
        log.debug("Fetching route with number: {}", routeNumber);
        Route route = routeRepository.findByRouteNumber(routeNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with number: " + routeNumber));
        return routeMapper.toDTO(route);
    }

    public Page<RouteDTO> listRoutes(Boolean isActive, Pageable pageable) {
        log.debug("Listing routes - isActive: {}, pageable: {}", isActive, pageable);
        Page<Route> routes;
        if (isActive != null) {
            routes = routeRepository.findByIsActive(isActive, pageable);
        } else {
            routes = routeRepository.findAll(pageable);
        }
        return routes.map(routeMapper::toDTO);
    }

    public List<RouteDTO> getRoutesByType(RouteType routeType) {
        log.debug("Fetching routes by type: {}", routeType);
        List<Route> routes = routeRepository.findByRouteTypeAndIsActive(routeType, true);
        return routeMapper.toDTOList(routes);
    }

    public List<RouteDTO> searchRoutes(String query) {
        log.debug("Searching routes with query: {}", query);
        List<Route> routes = routeRepository.searchRoutes(query);
        return routeMapper.toDTOList(routes);
    }

    public List<RouteDTO> getRoutesByStopId(Long stopId) {
        log.debug("Fetching routes for stop: {}", stopId);
        List<Route> routes = routeRepository.findByStopId(stopId);
        return routeMapper.toDTOList(routes);
    }

    @Transactional
    public RouteDTO createRoute(Route route) {
        log.info("Creating new route: {}", route.getRouteNumber());
        route.setIsActive(true);
        Route savedRoute = routeRepository.save(route);
        return routeMapper.toDTO(savedRoute);
    }

    @Transactional
    public RouteDTO updateRoute(Long id, Route routeUpdate) {
        log.info("Updating route with id: {}", id);
        Route existingRoute = routeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        
        // Update fields
        existingRoute.setName(routeUpdate.getName());
        existingRoute.setRouteType(routeUpdate.getRouteType());
        existingRoute.setTotalDistance(routeUpdate.getTotalDistance());
        existingRoute.setEstimatedDuration(routeUpdate.getEstimatedDuration());
        existingRoute.setColor(routeUpdate.getColor());
        existingRoute.setDescription(routeUpdate.getDescription());
        existingRoute.setPolyline(routeUpdate.getPolyline());
        
        Route updatedRoute = routeRepository.save(existingRoute);
        return routeMapper.toDTO(updatedRoute);
    }

    @Transactional
    public void deleteRoute(Long id) {
        log.info("Deleting route with id: {}", id);
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        route.setIsActive(false);
        routeRepository.save(route);
    }
}

