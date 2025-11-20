package com.bustransport.route.repository;

import com.bustransport.route.entity.Stop;
import com.bustransport.route.enums.StopType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {

    Optional<Stop> findByStopCode(String stopCode);

    List<Stop> findByIsActive(Boolean isActive);

    Page<Stop> findByIsActive(Boolean isActive, Pageable pageable);

    List<Stop> findByStopType(StopType stopType);

    @Query("SELECT s FROM Stop s WHERE s.isActive = true AND " +
           "(LOWER(s.stopCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Stop> searchStops(@Param("query") String query);

    // Find stops within a radius using the Haversine formula
    @Query(value = "SELECT * FROM stops s WHERE s.is_active = true AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(CAST(s.latitude AS double precision))) * " +
           "cos(radians(CAST(s.longitude AS double precision)) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(CAST(s.latitude AS double precision))))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(CAST(s.latitude AS double precision))) * " +
           "cos(radians(CAST(s.longitude AS double precision)) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(CAST(s.latitude AS double precision)))))",
           nativeQuery = true)
    List<Stop> findNearbyStops(
        @Param("lat") BigDecimal latitude,
        @Param("lon") BigDecimal longitude,
        @Param("radiusKm") double radiusKm
    );

    @Query("SELECT s FROM Stop s " +
           "JOIN s.routeStops rs " +
           "WHERE rs.route.id = :routeId " +
           "ORDER BY rs.stopSequence")
    List<Stop> findByRouteId(@Param("routeId") Long routeId);
}

