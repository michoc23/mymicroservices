package com.bustransport.geolocation.repository;

import com.bustransport.geolocation.entity.Bus;
import com.bustransport.geolocation.enums.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    
    Optional<Bus> findByBusNumber(String busNumber);
    
    Optional<Bus> findByDeviceId(String deviceId);
    
    List<Bus> findByStatus(BusStatus status);
    
    List<Bus> findByRouteId(Long routeId);
    
    List<Bus> findByRouteIdAndStatus(Long routeId, BusStatus status);
}

