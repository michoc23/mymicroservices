package com.bustransport.route.repository;

import com.bustransport.route.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByRouteIdOrderByStopSequence(Long routeId);

    List<RouteStop> findByStopId(Long stopId);

    @Query("SELECT rs FROM RouteStop rs WHERE rs.route.id = :routeId AND rs.stop.id = :stopId")
    Optional<RouteStop> findByRouteIdAndStopId(@Param("routeId") Long routeId, @Param("stopId") Long stopId);

    @Query("SELECT COUNT(rs) FROM RouteStop rs WHERE rs.route.id = :routeId")
    Integer countByRouteId(@Param("routeId") Long routeId);
}

