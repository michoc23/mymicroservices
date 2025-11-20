package com.bustransport.geolocation.repository;

import com.bustransport.geolocation.entity.GeofenceAlert;
import com.bustransport.geolocation.enums.AlertSeverity;
import com.bustransport.geolocation.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GeofenceAlertRepository extends JpaRepository<GeofenceAlert, Long> {
    
    @Query("SELECT ga FROM GeofenceAlert ga WHERE ga.acknowledgedAt IS NULL")
    List<GeofenceAlert> findActiveAlerts();
    
    List<GeofenceAlert> findByBusIdAndAcknowledgedAtIsNull(Long busId);
    
    List<GeofenceAlert> findByBusIdAndTriggeredAtBetween(
        Long busId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<GeofenceAlert> findByAlertTypeAndAcknowledgedAtIsNull(AlertType alertType);
    
    List<GeofenceAlert> findBySeverityAndAcknowledgedAtIsNull(AlertSeverity severity);
    
    @Query("SELECT ga FROM GeofenceAlert ga WHERE ga.triggeredAt >= :fromTime " +
           "ORDER BY ga.severity DESC, ga.triggeredAt DESC")
    List<GeofenceAlert> findRecentAlerts(@Param("fromTime") LocalDateTime fromTime);
}

