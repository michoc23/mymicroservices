package com.bustransport.ticket.service;

import com.bustransport.ticket.dto.request.CreateOrderRequest;
import com.bustransport.ticket.dto.request.CreateTicketRequest;
import com.bustransport.ticket.dto.response.OrderResponse;
import com.bustransport.ticket.entity.Order;
import com.bustransport.ticket.entity.Ticket;
import com.bustransport.ticket.enums.OrderStatus;
import com.bustransport.ticket.enums.TicketStatus;
import com.bustransport.ticket.enums.TicketType;
import com.bustransport.ticket.exception.ResourceNotFoundException;
import com.bustransport.ticket.mapper.OrderMapper;
import com.bustransport.ticket.repository.OrderRepository;
import com.bustransport.ticket.repository.TicketRepository;
import com.bustransport.ticket.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final OrderMapper orderMapper;
    private final QRCodeGenerator qrCodeGenerator;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Generate unique order number
        String orderNumber = generateOrderNumber();

        // Calculate total amount based on ticket types
        BigDecimal totalAmount = calculateTotalAmount(request.getTickets());

        // Create order
        Order order = Order.builder()
            .userId(request.getUserId())
            .orderNumber(orderNumber)
            .totalAmount(totalAmount)
            .status(OrderStatus.PENDING)
            .build();

        // Create tickets
        for (CreateTicketRequest ticketRequest : request.getTickets()) {
            Ticket ticket = createTicketFromRequest(ticketRequest, request.getUserId());
            order.addTicket(ticket);
        }

        order = orderRepository.save(order);

        // Generate QR codes for tickets after saving (need ticket IDs)
        for (Ticket ticket : order.getTickets()) {
            String qrCode = qrCodeGenerator.generateUniqueQRCode(ticket.getId(), ticket.getUserId());
            ticket.setQrCode(qrCode);
        }

        order = orderRepository.save(order);

        log.info("Order created with order number: {}", orderNumber);

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse markOrderAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(OrderStatus.PAID);

        // Activate all tickets
        for (Ticket ticket : order.getTickets()) {
            ticket.setStatus(TicketStatus.ACTIVE);
            ticket.setPurchaseDate(LocalDateTime.now());
        }

        order = orderRepository.save(order);

        log.info("Order {} marked as paid", orderId);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Cannot cancel paid order. Please request a refund instead.");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Cancel all tickets
        for (Ticket ticket : order.getTickets()) {
            if (ticket.getStatus() == TicketStatus.ACTIVE) {
                ticket.setStatus(TicketStatus.CANCELLED);
            }
        }

        order = orderRepository.save(order);

        log.info("Order {} cancelled", orderId);

        return orderMapper.toResponse(order);
    }

    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    private BigDecimal calculateTotalAmount(List<CreateTicketRequest> tickets) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateTicketRequest ticket : tickets) {
            total = total.add(getTicketPrice(ticket.getTicketType()));
        }
        return total;
    }

    private BigDecimal getTicketPrice(TicketType ticketType) {
        // This would ideally come from a configuration or pricing service
        return switch (ticketType) {
            case SINGLE -> new BigDecimal("2.50");
            case RETURN -> new BigDecimal("4.50");
            case DAY_PASS -> new BigDecimal("10.00");
            case MULTI_RIDE -> new BigDecimal("20.00");
        };
    }

    private Ticket createTicketFromRequest(CreateTicketRequest request, Long userId) {
        BigDecimal price = getTicketPrice(request.getTicketType());

        Integer maxUsage = request.getMaxUsage();
        if (maxUsage == null) {
            maxUsage = getDefaultMaxUsage(request.getTicketType());
        }

        return Ticket.builder()
            .userId(userId)
            .ticketType(request.getTicketType())
            .price(price)
            .validFrom(request.getValidFrom())
            .validUntil(request.getValidUntil())
            .status(TicketStatus.ACTIVE)
            .usageCount(0)
            .maxUsage(maxUsage)
            .routeId(request.getRouteId())
            .scheduleId(request.getScheduleId())
            .passengerName(request.getPassengerName())
            .build();
    }

    private Integer getDefaultMaxUsage(TicketType ticketType) {
        return switch (ticketType) {
            case SINGLE -> 1;
            case RETURN -> 2;
            case DAY_PASS -> Integer.MAX_VALUE;
            case MULTI_RIDE -> 10;
        };
    }
}
