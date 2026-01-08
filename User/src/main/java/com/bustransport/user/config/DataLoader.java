package com.bustransport.user.config;

import com.bustransport.user.entity.User;
import com.bustransport.user.entity.UserRole;
import com.bustransport.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Check if controller user already exists
            if (userRepository.findByEmail("controller@test.com").isEmpty()) {
                // Create test controller user
                User controller = User.builder()
                        .firstName("Test")
                        .lastName("Controller")
                        .email("controller@test.com")
                        .passwordHash(passwordEncoder.encode("test123"))
                        .phoneNumber("+1234567890")
                        .role(UserRole.CONTROLLER)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                userRepository.save(controller);
                log.info("✅ Test controller user created: controller@test.com / test123");
            } else {
                log.info("ℹ️ Test controller user already exists");
            }
        };
    }
}
