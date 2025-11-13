package com.bustransport.ticket.repository;

import com.bustransport.ticket.entity.Refund;
import com.bustransport.ticket.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByTransactionId(String transactionId);

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByRefundStatus(RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.refundStatus = 'PENDING' AND r.createdAt < :date")
    List<Refund> findPendingRefundsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Refund r WHERE r.payment.userId = :userId")
    List<Refund> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Refund r WHERE r.payment.userId = :userId AND r.refundStatus = :status")
    List<Refund> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RefundStatus status);

    @Query("SELECT SUM(r.refundAmount) FROM Refund r WHERE r.payment.id = :paymentId AND r.refundStatus = 'COMPLETED'")
    Double getTotalRefundedAmountByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT COUNT(r) FROM Refund r WHERE r.payment.userId = :userId AND r.refundStatus = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RefundStatus status);

    boolean existsByTransactionId(String transactionId);
}
