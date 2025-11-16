package com.bustransport.subscription.service;

import com.bustransport.subscription.entity.Subscription;
import com.bustransport.subscription.entity.SubscriptionUsage;
import com.bustransport.subscription.exception.ResourceNotFoundException;
import com.bustransport.subscription.repository.SubscriptionRepository;
import com.bustransport.subscription.repository.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionUsageService {

    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionUsage recordUsage(Long subscriptionId, Long routeId, Long busId) {
        log.info("Recording usage for subscription: {} on route: {} and bus: {}", subscriptionId, routeId, busId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        if (!subscription.isActive()) {
            throw new IllegalStateException("Cannot record usage for inactive subscription");
        }

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .subscription(subscription)
                .usageDate(LocalDateTime.now())
                .routeId(routeId)
                .busId(busId)
                .build();

        SubscriptionUsage savedUsage = subscriptionUsageRepository.save(usage);
        log.info("Recorded usage with id: {}", savedUsage.getId());
        
        return savedUsage;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionUsage> getUsageBySubscriptionId(Long subscriptionId) {
        return subscriptionUsageRepository.findBySubscriptionId(subscriptionId);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionUsage> getUsageBySubscriptionId(Long subscriptionId, Pageable pageable) {
        return subscriptionUsageRepository.findBySubscriptionId(subscriptionId, pageable);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionUsage> getUsageByDateRange(Long subscriptionId, LocalDateTime startDate, LocalDateTime endDate) {
        return subscriptionUsageRepository.findBySubscriptionIdAndUsageDateBetween(subscriptionId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public int getUsageCountByDateRange(Long subscriptionId, LocalDateTime startDate, LocalDateTime endDate) {
        return subscriptionUsageRepository.countUsageBySubscriptionIdAndDateRange(subscriptionId, startDate, endDate);
    }
}