package com.bustransport.ticket.repository;

import com.bustransport.ticket.entity.Payment;
import com.bustransport.ticket.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findBySubscriptionId(Long subscriptionId);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate < :date")
    List<Payment> findCompletedPaymentsBefore(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Double getTotalAmountByUserId(@Param("userId") Long userId);

    boolean existsByTransactionId(String transactionId);
}
