package com.bustransport.ticket.repository;

import com.bustransport.ticket.entity.Ticket;
import com.bustransport.ticket.enums.TicketStatus;
import com.bustransport.ticket.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByQrCode(String qrCode);

    List<Ticket> findByUserId(Long userId);

    List<Ticket> findByUserIdAndStatus(Long userId, TicketStatus status);

    List<Ticket> findByOrderId(Long orderId);

    List<Ticket> findByRouteId(Long routeId);

    List<Ticket> findByScheduleId(Long scheduleId);

    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.status = 'ACTIVE' AND t.validUntil > :now")
    List<Ticket> findActiveTicketsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.ticketType = :type AND t.validFrom <= :now AND t.validUntil > :now AND t.status = 'ACTIVE'")
    List<Ticket> findValidTicketsByUserIdAndType(
        @Param("userId") Long userId,
        @Param("type") TicketType type,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT t FROM Ticket t WHERE t.status = 'ACTIVE' AND t.validUntil < :expiryDate")
    List<Ticket> findExpiredTickets(@Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.userId = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TicketStatus status);

    boolean existsByQrCode(String qrCode);
}
