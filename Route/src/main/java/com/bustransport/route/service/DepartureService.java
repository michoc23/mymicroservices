package com.bustransport.route.service;

import com.bustransport.route.dto.request.NextDeparturesRequest;
import com.bustransport.route.dto.response.DepartureDTO;
import com.bustransport.route.dto.response.NextDeparturesResponse;
import com.bustransport.route.entity.Departure;
import com.bustransport.route.enums.DepartureStatus;
import com.bustransport.route.exception.ResourceNotFoundException;
import com.bustransport.route.mapper.DepartureMapper;
import com.bustransport.route.repository.DepartureRepository;
import com.bustransport.route.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartureService {

    private final DepartureRepository departureRepository;
    private final StopRepository stopRepository;
    private final DepartureMapper departureMapper;

    public DepartureDTO getDepartureById(Long id) {
        log.debug("Fetching departure with id: {}", id);
        Departure departure = departureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Departure not found with id: " + id));
        return departureMapper.toDTO(departure);
    }

    public NextDeparturesResponse getNextDepartures(NextDeparturesRequest request) {
        log.debug("Fetching next departures for stop: {}", request.getStopId());
        
        var stop = stopRepository.findById(request.getStopId())
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + request.getStopId()));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMinutes(request.getTimeWindowMinutes());
        
        List<Departure> departures;
        if (request.getRouteId() != null) {
            departures = departureRepository.findUpcomingDeparturesByStopIdAndRouteId(
                request.getStopId(), 
                request.getRouteId(), 
                now, 
                endTime
            );
        } else {
            departures = departureRepository.findUpcomingDeparturesByStopId(
                request.getStopId(), 
                now, 
                endTime
            );
        }
        
        // Limit results
        if (departures.size() > request.getLimit()) {
            departures = departures.subList(0, request.getLimit());
        }
        
        List<DepartureDTO> departureDTOs = departureMapper.toDTOList(departures);
        
        return NextDeparturesResponse.builder()
            .stopName(stop.getName())
            .queryTime(now)
            .departures(departureDTOs)
            .hasRealTimeData(stop.getHasRealTimeInfo())
            .build();
    }

    public List<DepartureDTO> getDeparturesByStatus(DepartureStatus status) {
        log.debug("Fetching departures by status: {}", status);
        List<Departure> departures = departureRepository.findByStatus(status);
        return departureMapper.toDTOList(departures);
    }

    public List<DepartureDTO> getDeparturesByRouteAndTimeRange(
            Long routeId, 
            LocalDateTime fromTime, 
            LocalDateTime toTime) {
        log.debug("Fetching departures for route: {} between {} and {}", routeId, fromTime, toTime);
        List<Departure> departures = departureRepository.findByRouteIdAndTimeRange(routeId, fromTime, toTime);
        return departureMapper.toDTOList(departures);
    }

    @Transactional
    public DepartureDTO updateDepartureStatus(Long id, DepartureStatus status, Integer delayMinutes, String message) {
        log.info("Updating departure {} status to {} with delay: {} minutes", id, status, delayMinutes);
        Departure departure = departureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Departure not found with id: " + id));
        
        departure.setStatus(status);
        departure.setDelayMinutes(delayMinutes != null ? delayMinutes : 0);
        departure.setStatusMessage(message);
        
        Departure updatedDeparture = departureRepository.save(departure);
        
        // Here we could publish an event to Notification Service
        // publishDepartureStatusEvent(updatedDeparture);
        
        return departureMapper.toDTO(updatedDeparture);
    }

    @Transactional
    public DepartureDTO createDeparture(Departure departure) {
        log.info("Creating new departure");
        if (departure.getStatus() == null) {
            departure.setStatus(DepartureStatus.ON_TIME);
        }
        if (departure.getDelayMinutes() == null) {
            departure.setDelayMinutes(0);
        }
        Departure savedDeparture = departureRepository.save(departure);
        return departureMapper.toDTO(savedDeparture);
    }

    @Transactional
    public void deleteDeparture(Long id) {
        log.info("Deleting departure with id: {}", id);
        Departure departure = departureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Departure not found with id: " + id));
        departure.setStatus(DepartureStatus.CANCELLED);
        departureRepository.save(departure);
    }
}

