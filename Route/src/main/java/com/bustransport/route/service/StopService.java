package com.bustransport.route.service;

import com.bustransport.route.dto.response.StopDTO;
import com.bustransport.route.entity.Stop;
import com.bustransport.route.exception.ResourceNotFoundException;
import com.bustransport.route.mapper.StopMapper;
import com.bustransport.route.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StopService {

    private final StopRepository stopRepository;
    private final StopMapper stopMapper;

    @Cacheable(value = "stops", key = "#id")
    public StopDTO getStopById(Long id) {
        log.debug("Fetching stop with id: {}", id);
        Stop stop = stopRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + id));
        return stopMapper.toDTO(stop);
    }

    @Cacheable(value = "stops", key = "#stopCode")
    public StopDTO getStopByCode(String stopCode) {
        log.debug("Fetching stop with code: {}", stopCode);
        Stop stop = stopRepository.findByStopCode(stopCode)
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with code: " + stopCode));
        return stopMapper.toDTO(stop);
    }

    public Page<StopDTO> listStops(Boolean isActive, Pageable pageable) {
        log.debug("Listing stops - isActive: {}, pageable: {}", isActive, pageable);
        Page<Stop> stops;
        if (isActive != null) {
            stops = stopRepository.findByIsActive(isActive, pageable);
        } else {
            stops = stopRepository.findAll(pageable);
        }
        return stops.map(stopMapper::toDTO);
    }

    public List<StopDTO> searchStops(String query) {
        log.debug("Searching stops with query: {}", query);
        List<Stop> stops = stopRepository.searchStops(query);
        return stopMapper.toDTOList(stops);
    }

    @Cacheable(value = "nearbyStops", key = "#latitude + '_' + #longitude + '_' + #radiusKm")
    public List<StopDTO> findNearbyStops(BigDecimal latitude, BigDecimal longitude, double radiusKm) {
        log.debug("Finding stops near ({}, {}) within {} km", latitude, longitude, radiusKm);
        List<Stop> stops = stopRepository.findNearbyStops(latitude, longitude, radiusKm);
        return stopMapper.toDTOList(stops);
    }

    @Cacheable(value = "routeStops", key = "#routeId")
    public List<StopDTO> getStopsByRoute(Long routeId) {
        log.debug("Fetching stops for route: {}", routeId);
        List<Stop> stops = stopRepository.findByRouteId(routeId);
        return stopMapper.toDTOList(stops);
    }

    @Transactional
    public StopDTO createStop(Stop stop) {
        log.info("Creating new stop: {}", stop.getStopCode());
        stop.setIsActive(true);
        Stop savedStop = stopRepository.save(stop);
        return stopMapper.toDTO(savedStop);
    }

    @Transactional
    public StopDTO updateStop(Long id, Stop stopUpdate) {
        log.info("Updating stop with id: {}", id);
        Stop existingStop = stopRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + id));
        
        existingStop.setName(stopUpdate.getName());
        existingStop.setLatitude(stopUpdate.getLatitude());
        existingStop.setLongitude(stopUpdate.getLongitude());
        existingStop.setAddress(stopUpdate.getAddress());
        existingStop.setStopType(stopUpdate.getStopType());
        existingStop.setHasWheelchairAccess(stopUpdate.getHasWheelchairAccess());
        existingStop.setHasShelter(stopUpdate.getHasShelter());
        existingStop.setHasRealTimeInfo(stopUpdate.getHasRealTimeInfo());
        existingStop.setZone(stopUpdate.getZone());
        
        Stop updatedStop = stopRepository.save(existingStop);
        return stopMapper.toDTO(updatedStop);
    }

    @Transactional
    public void deleteStop(Long id) {
        log.info("Deleting stop with id: {}", id);
        Stop stop = stopRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + id));
        stop.setIsActive(false);
        stopRepository.save(stop);
    }
}

