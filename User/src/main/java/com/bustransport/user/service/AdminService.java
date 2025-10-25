package com.bustransport.user.service;

import com.bustransport.user.dto.UserResponse;
import com.bustransport.user.entity.User;
import com.bustransport.user.entity.UserRole;
import com.bustransport.user.exception.BadRequestException;
import com.bustransport.user.exception.ResourceNotFoundException;
import com.bustransport.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    public Page<UserResponse> searchUsers(String email, String role, Pageable pageable) {
        return userRepository.searchUsers(email, role, pageable)
                .map(this::mapToUserResponse);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("Admin activated user: {}", user.getEmail());
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Admin deactivated user: {}", user.getEmail());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
        log.info("Admin deleted user: {}", user.getEmail());
    }

    @Transactional
    public void updateUserRole(Long userId, String roleStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            UserRole role = UserRole.valueOf(roleStr.toUpperCase());
            user.setRole(role);
            userRepository.save(user);
            log.info("Admin updated user role to {}: {}", role, user.getEmail());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + roleStr);
        }
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