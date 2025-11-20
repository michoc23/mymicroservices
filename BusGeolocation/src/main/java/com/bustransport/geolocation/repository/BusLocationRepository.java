package com.bustransport.geolocation.repository;

import com.bustransport.geolocation.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusLocationRepository extends JpaRepository<BusLocation, Long> {
    
    @Query("SELECT bl FROM BusLocation bl WHERE bl.bus.id = :busId ORDER BY bl.recordedAt DESC LIMIT 1")
    Optional<BusLocation> findLatestByBusId(@Param("busId") Long busId);
    
    List<BusLocation> findByBusIdAndRecordedAtBetween(
        Long busId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT bl FROM BusLocation bl WHERE bl.recordedAt >= :sinceTime " +
           "AND bl.id IN (SELECT MAX(bl2.id) FROM BusLocation bl2 GROUP BY bl2.bus.id)")
    List<BusLocation> findAllLatestLocations(@Param("sinceTime") LocalDateTime sinceTime);
    
    void deleteByRecordedAtBefore(LocalDateTime cutoffTime);
}

