package com.bustransport.subscription.service;

import com.bustransport.subscription.dto.request.CreateSubscriptionRequest;
import com.bustransport.subscription.dto.request.UpdateSubscriptionRequest;
import com.bustransport.subscription.dto.response.SubscriptionResponse;
import com.bustransport.subscription.entity.Subscription;
import com.bustransport.subscription.enums.SubscriptionStatus;
import com.bustransport.subscription.enums.SubscriptionType;
import com.bustransport.subscription.exception.ResourceNotFoundException;
import com.bustransport.subscription.exception.SubscriptionException;
import com.bustransport.subscription.mapper.SubscriptionMapper;
import com.bustransport.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    // Pricing constants
    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("29.99");
    private static final BigDecimal ANNUAL_PRICE = new BigDecimal("299.99");

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByUserId(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getSubscriptionsByUserId(Long userId, Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionRepository.findByUserId(userId, pageable);
        return subscriptions.map(subscriptionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> getActiveSubscriptionByUserId(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        return subscription.map(subscriptionMapper::toResponse);
    }

    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {}", request.getUserId());

        // Check if user already has an active subscription
        boolean hasActiveSubscription = subscriptionRepository.existsByUserIdAndStatus(
                request.getUserId(), SubscriptionStatus.ACTIVE);
        
        if (hasActiveSubscription) {
            throw new SubscriptionException("User already has an active subscription");
        }

        Subscription subscription = subscriptionMapper.toEntity(request);
        
        // Set pricing based on subscription type
        subscription.setPrice(getPrice(request.getSubscriptionType()));
        
        // Set dates
        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        subscription.setEndDate(calculateEndDate(now, request.getSubscriptionType()));
        
        // Set initial status
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Created subscription with id: {} for user: {}", savedSubscription.getId(), request.getUserId());
        
        return subscriptionMapper.toResponse(savedSubscription);
    }

    public SubscriptionResponse updateSubscription(Long id, UpdateSubscriptionRequest request) {
        log.info("Updating subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (request.getAutoRenewal() != null) {
            subscription.setAutoRenewal(request.getAutoRenewal());
        }

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Updated subscription: {}", id);
        
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    public SubscriptionResponse renewSubscription(Long id) {
        log.info("Renewing subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionException("Cannot renew cancelled subscription");
        }

        subscription.renew();
        Subscription renewedSubscription = subscriptionRepository.save(subscription);
        log.info("Renewed subscription: {}", id);
        
        return subscriptionMapper.toResponse(renewedSubscription);
    }

    public SubscriptionResponse cancelSubscription(Long id, String reason) {
        log.info("Cancelling subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (!subscription.canBeCancelled()) {
            throw new SubscriptionException("Subscription cannot be cancelled");
        }

        subscription.cancel(reason);
        Subscription cancelledSubscription = subscriptionRepository.save(subscription);
        log.info("Cancelled subscription: {}", id);
        
        return subscriptionMapper.toResponse(cancelledSubscription);
    }

    public SubscriptionResponse suspendSubscription(Long id) {
        log.info("Suspending subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        subscription.suspend();
        Subscription suspendedSubscription = subscriptionRepository.save(subscription);
        log.info("Suspended subscription: {}", id);
        
        return subscriptionMapper.toResponse(suspendedSubscription);
    }

    public SubscriptionResponse reactivateSubscription(Long id) {
        log.info("Reactivating subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        subscription.reactivate();
        Subscription reactivatedSubscription = subscriptionRepository.save(subscription);
        log.info("Reactivated subscription: {}", id);
        
        return subscriptionMapper.toResponse(reactivatedSubscription);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    public void processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");
        
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredSubscriptions(LocalDateTime.now(), SubscriptionStatus.ACTIVE);
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Marked subscription {} as expired", subscription.getId());
        }
        
        log.info("Processed {} expired subscriptions", expiredSubscriptions.size());
    }

    public void processAutoRenewals() {
        log.info("Processing auto-renewals");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalWindow = now.plusDays(1); // Renew 1 day before expiry
        
        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findSubscriptionsForRenewal(now, renewalWindow, SubscriptionStatus.ACTIVE);
        
        for (Subscription subscription : subscriptionsToRenew) {
            try {
                subscription.renew();
                subscriptionRepository.save(subscription);
                log.info("Auto-renewed subscription {}", subscription.getId());
            } catch (Exception e) {
                log.error("Failed to auto-renew subscription {}: {}", subscription.getId(), e.getMessage());
                subscription.setAutoRenewal(false); // Disable auto-renewal on failure
                subscriptionRepository.save(subscription);
            }
        }
        
        log.info("Processed {} auto-renewals", subscriptionsToRenew.size());
    }

    private BigDecimal getPrice(SubscriptionType type) {
        return switch (type) {
            case MONTHLY -> MONTHLY_PRICE;
            case ANNUAL -> ANNUAL_PRICE;
        };
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, SubscriptionType type) {
        return switch (type) {
            case MONTHLY -> startDate.plusMonths(1);
            case ANNUAL -> startDate.plusYears(1);
        };
    }
}