package com.bustransport.user.service;

import com.bustransport.user.dto.ChangePasswordRequest;
import com.bustransport.user.dto.TicketConsumptionResponse;
import com.bustransport.user.dto.UpdateProfileRequest;
import com.bustransport.user.dto.UserResponse;
import com.bustransport.user.entity.User;
import com.bustransport.user.exception.BadRequestException;
import com.bustransport.user.exception.ResourceNotFoundException;
import com.bustransport.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${services.ticket.url:http://ticket-service:8083/api/v1}")
    private String ticketServiceUrl;

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated: {}", updatedUser.getEmail());

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
        log.info("User account deleted: {}", user.getEmail());
    }

    @Transactional
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(true);
        userRepository.save(user);
        log.info("User account activated: {}", user.getEmail());
    }

    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(false);
        userRepository.save(user);
        log.info("User account deactivated: {}", user.getEmail());
    }

    /**
     * Check if a ticket is consumed or not
     * @param userId The user ID to verify ownership
     * @param ticketId The ticket ID to check
     * @return TicketConsumptionResponse with consumption status
     */
    public TicketConsumptionResponse checkTicketConsumption(Long userId, Long ticketId) {
        try {
            // Call ticket service to get ticket details
            String url = ticketServiceUrl + "/tickets/" + ticketId;
            log.debug("Calling ticket service: {}", url);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> ticketData = response.getBody();
                
                // Extract ticket information
                Long ticketUserId = getLongValue(ticketData, "userId");
                Long ticketIdValue = getLongValue(ticketData, "id");
                String statusStr = (String) ticketData.get("status");
                String qrCode = (String) ticketData.get("qrCode");
                Integer usageCount = getIntegerValue(ticketData, "usageCount");
                Integer maxUsage = getIntegerValue(ticketData, "maxUsage");
                LocalDateTime validUntil = parseDateTime(ticketData.get("validUntil"));
                Long routeId = getLongValue(ticketData, "routeId");
                
                // Verify ticket belongs to the user
                if (!ticketUserId.equals(userId)) {
                    throw new BadRequestException("Ticket does not belong to this user");
                }
                
                // Determine if ticket is consumed
                boolean isConsumed = "USED".equals(statusStr) 
                    || (usageCount != null && maxUsage != null && usageCount >= maxUsage);
                
                // Check if ticket is still valid
                boolean isValid = "ACTIVE".equals(statusStr)
                    && validUntil != null
                    && LocalDateTime.now().isBefore(validUntil)
                    && usageCount != null && maxUsage != null
                    && usageCount < maxUsage;
                
                String message = buildConsumptionMessage(statusStr, usageCount, maxUsage, validUntil, isConsumed, isValid);
                
                return TicketConsumptionResponse.builder()
                    .ticketId(ticketIdValue)
                    .qrCode(qrCode)
                    .status(statusStr)
                    .isConsumed(isConsumed)
                    .isValid(isValid)
                    .usageCount(usageCount)
                    .maxUsage(maxUsage)
                    .validUntil(validUntil)
                    .message(message)
                    .userId(ticketUserId)
                    .routeId(routeId)
                    .build();
            } else {
                throw new ResourceNotFoundException("Ticket not found with id: " + ticketId);
            }
            
        } catch (RestClientException e) {
            log.error("Error calling ticket service: {}", e.getMessage(), e);
            throw new BadRequestException("Unable to check ticket status. Ticket service may be unavailable.");
        }
    }

    /**
     * Check ticket consumption by QR code
     * @param userId The user ID to verify ownership
     * @param qrCode The QR code of the ticket
     * @return TicketConsumptionResponse with consumption status
     */
    public TicketConsumptionResponse checkTicketConsumptionByQrCode(Long userId, String qrCode) {
        try {
            // Call ticket service to get ticket by QR code
            String url = ticketServiceUrl + "/tickets/qr/" + qrCode;
            log.debug("Calling ticket service: {}", url);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> ticketData = response.getBody();
                
                // Extract ticket information
                Long ticketUserId = getLongValue(ticketData, "userId");
                Long ticketIdValue = getLongValue(ticketData, "id");
                String statusStr = (String) ticketData.get("status");
                String qrCodeValue = (String) ticketData.get("qrCode");
                Integer usageCount = getIntegerValue(ticketData, "usageCount");
                Integer maxUsage = getIntegerValue(ticketData, "maxUsage");
                LocalDateTime validUntil = parseDateTime(ticketData.get("validUntil"));
                Long routeId = getLongValue(ticketData, "routeId");
                
                // Verify ticket belongs to the user
                if (!ticketUserId.equals(userId)) {
                    throw new BadRequestException("Ticket does not belong to this user");
                }
                
                // Determine if ticket is consumed
                boolean isConsumed = "USED".equals(statusStr) 
                    || (usageCount != null && maxUsage != null && usageCount >= maxUsage);
                
                // Check if ticket is still valid
                boolean isValid = "ACTIVE".equals(statusStr)
                    && validUntil != null
                    && LocalDateTime.now().isBefore(validUntil)
                    && usageCount != null && maxUsage != null
                    && usageCount < maxUsage;
                
                String message = buildConsumptionMessage(statusStr, usageCount, maxUsage, validUntil, isConsumed, isValid);
                
                return TicketConsumptionResponse.builder()
                    .ticketId(ticketIdValue)
                    .qrCode(qrCodeValue)
                    .status(statusStr)
                    .isConsumed(isConsumed)
                    .isValid(isValid)
                    .usageCount(usageCount)
                    .maxUsage(maxUsage)
                    .validUntil(validUntil)
                    .message(message)
                    .userId(ticketUserId)
                    .routeId(routeId)
                    .build();
            } else {
                throw new ResourceNotFoundException("Ticket not found with QR code: " + qrCode);
            }
            
        } catch (RestClientException e) {
            log.error("Error calling ticket service: {}", e.getMessage(), e);
            throw new BadRequestException("Unable to check ticket status. Ticket service may be unavailable.");
        }
    }

    private String buildConsumptionMessage(String statusStr, Integer usageCount, Integer maxUsage, 
                                          LocalDateTime validUntil, boolean isConsumed, boolean isValid) {
        if (isConsumed) {
            if ("USED".equals(statusStr)) {
                return "Ticket has been used";
            } else if (usageCount != null && maxUsage != null && usageCount >= maxUsage) {
                return "Ticket has reached maximum usage limit (" + usageCount + "/" + maxUsage + ")";
            }
        }
        
        if (!isValid) {
            if ("EXPIRED".equals(statusStr)) {
                return "Ticket has expired";
            } else if ("CANCELLED".equals(statusStr)) {
                return "Ticket has been cancelled";
            } else if (validUntil != null && LocalDateTime.now().isAfter(validUntil)) {
                return "Ticket validity period has ended";
            }
        }
        
        if (isValid && !isConsumed && usageCount != null && maxUsage != null) {
            return "Ticket is valid and available for use (" + usageCount + "/" + maxUsage + " uses)";
        }
        
        return "Ticket status: " + (statusStr != null ? statusStr.toLowerCase() : "unknown");
    }

    // Helper methods to parse response data
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Integer getIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                log.warn("Failed to parse date: {}", value);
                return null;
            }
        }
        return null;
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}