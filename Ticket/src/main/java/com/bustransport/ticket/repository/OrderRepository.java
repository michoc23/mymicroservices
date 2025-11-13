package com.bustransport.ticket.repository;

import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);
}
