package com.bustransport.subscription.repository;

import com.bustransport.subscription.entity.Subscription;
import com.bustransport.subscription.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);
    
    Page<Subscription> findByUserId(Long userId, Pageable pageable);
    
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :date AND s.status = :status")
    List<Subscription> findExpiredSubscriptions(@Param("date") LocalDateTime date, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :startDate AND :endDate AND s.autoRenewal = true AND s.status = :status")
    List<Subscription> findSubscriptionsForRenewal(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate, 
                                                   @Param("status") SubscriptionStatus status);
    
    boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    int countActiveSubscriptionsByUserId(@Param("userId") Long userId);
}