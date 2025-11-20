package com.bustransport.route.repository;

import com.bustransport.route.entity.Route;
import com.bustransport.route.enums.RouteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByRouteNumber(String routeNumber);

    List<Route> findByRouteTypeAndIsActive(RouteType routeType, Boolean isActive);

    Page<Route> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT r FROM Route r WHERE r.isActive = true AND " +
           "(LOWER(r.routeNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Route> searchRoutes(@Param("query") String query);

    List<Route> findByRouteType(RouteType routeType);

    @Query("SELECT DISTINCT r FROM Route r " +
           "JOIN r.routeStops rs " +
           "JOIN rs.stop s " +
           "WHERE s.id = :stopId AND r.isActive = true")
    List<Route> findByStopId(@Param("stopId") Long stopId);
}

