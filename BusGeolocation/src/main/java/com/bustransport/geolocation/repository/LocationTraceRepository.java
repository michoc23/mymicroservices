package com.bustransport.geolocation.repository;

import com.bustransport.geolocation.entity.LocationTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationTraceRepository extends JpaRepository<LocationTrace, Long> {
    
    List<LocationTrace> findByBusIdAndStartTimeBetween(
        Long busId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<LocationTrace> findByRouteIdAndStartTimeBetween(
        Long routeId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT lt FROM LocationTrace lt WHERE lt.bus.id = :busId " +
           "ORDER BY lt.startTime DESC LIMIT :limit")
    List<LocationTrace> findRecentTracesByBusId(
        @Param("busId") Long busId, @Param("limit") int limit);
    
    void deleteByEndTimeBefore(LocalDateTime cutoffTime);
}

