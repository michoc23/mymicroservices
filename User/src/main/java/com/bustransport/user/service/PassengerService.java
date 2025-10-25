package com.bustransport.user.service;

import com.bustransport.user.dto.PassengerResponse;
import com.bustransport.user.entity.Passenger;
import com.bustransport.user.exception.ResourceNotFoundException;
import com.bustransport.user.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerResponse getPassengerByUserId(Long userId) {
        Passenger passenger = passengerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + userId));

        return mapToPassengerResponse(passenger);
    }

    public Integer getLoyaltyPoints(Long userId) {
        Passenger passenger = passengerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + userId));

        return passenger.getLoyaltyPoints();
    }

    @Transactional
    public void addLoyaltyPoints(Long userId, Integer points) {
        Passenger passenger = passengerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + userId));

        passenger.addPoints(points);
        passengerRepository.save(passenger);

        log.info("Added {} loyalty points to passenger: {}", points, passenger.getEmail());
    }

    @Transactional
    public void updatePreferredLanguage(Long userId, String language) {
        Passenger passenger = passengerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + userId));

        passenger.setPreferredLanguage(language);
        passengerRepository.save(passenger);

        log.info("Updated preferred language for passenger: {}", passenger.getEmail());
    }

    @Transactional
    public PassengerResponse updatePassenger(Long userId, UpdatePassengerRequest request) {
        Passenger passenger = passengerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + userId));

        if (request.getPreferredLanguage() != null) {
            passenger.setPreferredLanguage(request.getPreferredLanguage());
        }

        Passenger updatedPassenger = passengerRepository.save(passenger);
        log.info("Passenger profile updated: {}", updatedPassenger.getEmail());

        return mapToPassengerResponse(updatedPassenger);
    }

    private PassengerResponse mapToPassengerResponse(Passenger passenger) {
        PassengerResponse response = new PassengerResponse();
        response.setId(passenger.getId());
        response.setFirstName(passenger.getFirstName());
        response.setLastName(passenger.getLastName());
        response.setEmail(passenger.getEmail());
        response.setPhoneNumber(passenger.getPhoneNumber());
        response.setRole(passenger.getRole().name());
        response.setIsActive(passenger.getIsActive());
        response.setLastLoginAt(passenger.getLastLoginAt());
        response.setCreatedAt(passenger.getCreatedAt());
        response.setUpdatedAt(passenger.getUpdatedAt());
        response.setLoyaltyPoints(passenger.getLoyaltyPoints());
        response.setPreferredLanguage(passenger.getPreferredLanguage());
        return response;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdatePassengerRequest {
        private String preferredLanguage;
    }
}