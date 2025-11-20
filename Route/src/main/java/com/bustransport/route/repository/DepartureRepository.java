package com.bustransport.route.repository;

import com.bustransport.route.entity.Departure;
import com.bustransport.route.enums.DepartureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepartureRepository extends JpaRepository<Departure, Long> {

    @Query("SELECT d FROM Departure d WHERE d.stop.id = :stopId AND " +
           "d.departureTime >= :fromTime AND d.departureTime <= :toTime AND " +
           "d.status != com.bustransport.route.enums.DepartureStatus.CANCELLED " +
           "ORDER BY d.departureTime ASC")
    List<Departure> findUpcomingDeparturesByStopId(
        @Param("stopId") Long stopId,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime
    );

    @Query("SELECT d FROM Departure d WHERE d.stop.id = :stopId AND " +
           "d.schedule.route.id = :routeId AND " +
           "d.departureTime >= :fromTime AND d.departureTime <= :toTime AND " +
           "d.status != com.bustransport.route.enums.DepartureStatus.CANCELLED " +
           "ORDER BY d.departureTime ASC")
    List<Departure> findUpcomingDeparturesByStopIdAndRouteId(
        @Param("stopId") Long stopId,
        @Param("routeId") Long routeId,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime
    );

    List<Departure> findByStatus(DepartureStatus status);

    @Query("SELECT d FROM Departure d WHERE d.departureTime >= :fromTime AND " +
           "d.departureTime <= :toTime AND d.status = :status")
    List<Departure> findByStatusAndTimeRange(
        @Param("status") DepartureStatus status,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime
    );

    List<Departure> findByScheduleId(Long scheduleId);

    @Query("SELECT d FROM Departure d WHERE d.schedule.route.id = :routeId AND " +
           "d.departureTime >= :fromTime AND d.departureTime <= :toTime " +
           "ORDER BY d.departureTime ASC")
    List<Departure> findByRouteIdAndTimeRange(
        @Param("routeId") Long routeId,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime
    );
}

