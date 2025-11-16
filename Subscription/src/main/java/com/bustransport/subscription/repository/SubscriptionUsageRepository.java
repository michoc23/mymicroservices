package com.bustransport.subscription.repository;

import com.bustransport.subscription.entity.SubscriptionUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {

    List<SubscriptionUsage> findBySubscriptionId(Long subscriptionId);
    
    Page<SubscriptionUsage> findBySubscriptionId(Long subscriptionId, Pageable pageable);
    
    @Query("SELECT su FROM SubscriptionUsage su WHERE su.subscription.id = :subscriptionId AND su.usageDate BETWEEN :startDate AND :endDate")
    List<SubscriptionUsage> findBySubscriptionIdAndUsageDateBetween(@Param("subscriptionId") Long subscriptionId, 
                                                                     @Param("startDate") LocalDateTime startDate, 
                                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(su) FROM SubscriptionUsage su WHERE su.subscription.id = :subscriptionId AND su.usageDate BETWEEN :startDate AND :endDate")
    int countUsageBySubscriptionIdAndDateRange(@Param("subscriptionId") Long subscriptionId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
}