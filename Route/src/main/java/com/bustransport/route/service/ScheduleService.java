package com.bustransport.route.service;

import com.bustransport.route.dto.response.ScheduleDTO;
import com.bustransport.route.entity.Schedule;
import com.bustransport.route.enums.ServiceType;
import com.bustransport.route.exception.ResourceNotFoundException;
import com.bustransport.route.mapper.ScheduleMapper;
import com.bustransport.route.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    public ScheduleDTO getScheduleById(Long id) {
        log.debug("Fetching schedule with id: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return scheduleMapper.toDTO(schedule);
    }

    @Cacheable(value = "schedules", key = "#routeId + '_' + #serviceType")
    public List<ScheduleDTO> getSchedulesByRoute(Long routeId, ServiceType serviceType) {
        log.debug("Fetching schedules for route: {} and service type: {}", routeId, serviceType);
        List<Schedule> schedules;
        if (serviceType != null) {
            schedules = scheduleRepository.findByRouteIdAndServiceType(routeId, serviceType);
        } else {
            schedules = scheduleRepository.findByRouteId(routeId);
        }
        return scheduleMapper.toDTOList(schedules);
    }

    @Cacheable(value = "dailySchedules", key = "#routeId + '_' + #date")
    public List<ScheduleDTO> getDailySchedule(Long routeId, LocalDate date) {
        log.debug("Fetching daily schedule for route: {} on date: {}", routeId, date);
        ServiceType serviceType = determineServiceType(date);
        List<Schedule> schedules = scheduleRepository.findActiveSchedules(routeId, serviceType, date);
        return scheduleMapper.toDTOList(schedules);
    }

    public List<ScheduleDTO> getAllActiveSchedulesForDate(LocalDate date) {
        log.debug("Fetching all active schedules for date: {}", date);
        List<Schedule> schedules = scheduleRepository.findAllActiveSchedulesForDate(date);
        return scheduleMapper.toDTOList(schedules);
    }

    @Transactional
    public ScheduleDTO createSchedule(Schedule schedule) {
        log.info("Creating new schedule for route: {}", schedule.getRoute().getId());
        schedule.setIsActive(true);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDTO(savedSchedule);
    }

    @Transactional
    public ScheduleDTO updateSchedule(Long id, Schedule scheduleUpdate) {
        log.info("Updating schedule with id: {}", id);
        Schedule existingSchedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        existingSchedule.setServiceType(scheduleUpdate.getServiceType());
        existingSchedule.setStartTime(scheduleUpdate.getStartTime());
        existingSchedule.setEndTime(scheduleUpdate.getEndTime());
        existingSchedule.setFrequency(scheduleUpdate.getFrequency());
        existingSchedule.setFirstDeparture(scheduleUpdate.getFirstDeparture());
        existingSchedule.setLastDeparture(scheduleUpdate.getLastDeparture());
        existingSchedule.setValidFrom(scheduleUpdate.getValidFrom());
        existingSchedule.setValidUntil(scheduleUpdate.getValidUntil());
        existingSchedule.setNotes(scheduleUpdate.getNotes());
        
        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return scheduleMapper.toDTO(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule with id: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        schedule.setIsActive(false);
        scheduleRepository.save(schedule);
    }

    private ServiceType determineServiceType(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        // Simple logic - can be enhanced with holiday calendar
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return ServiceType.WEEKEND;
        }
        return ServiceType.WEEKDAY;
    }
}

