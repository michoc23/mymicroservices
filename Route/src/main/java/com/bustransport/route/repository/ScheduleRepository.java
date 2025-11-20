package com.bustransport.route.repository;

import com.bustransport.route.entity.Schedule;
import com.bustransport.route.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByRouteId(Long routeId);

    List<Schedule> findByRouteIdAndServiceType(Long routeId, ServiceType serviceType);

    @Query("SELECT s FROM Schedule s WHERE s.route.id = :routeId AND s.serviceType = :serviceType AND " +
           "s.isActive = true AND (s.validFrom IS NULL OR s.validFrom <= :date) AND " +
           "(s.validUntil IS NULL OR s.validUntil >= :date)")
    List<Schedule> findActiveSchedules(
        @Param("routeId") Long routeId,
        @Param("serviceType") ServiceType serviceType,
        @Param("date") LocalDate date
    );

    @Query("SELECT s FROM Schedule s WHERE s.isActive = true AND " +
           "(s.validFrom IS NULL OR s.validFrom <= :date) AND " +
           "(s.validUntil IS NULL OR s.validUntil >= :date)")
    List<Schedule> findAllActiveSchedulesForDate(@Param("date") LocalDate date);

    List<Schedule> findByServiceTypeAndIsActive(ServiceType serviceType, Boolean isActive);
}

