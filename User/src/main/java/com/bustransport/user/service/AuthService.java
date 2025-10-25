package com.bustransport.user.service;

import com.bustransport.user.dto.AuthResponse;
import com.bustransport.user.dto.LoginRequest;
import com.bustransport.user.dto.RegisterRequest;
import com.bustransport.user.entity.*;
import com.bustransport.user.exception.BadRequestException;
import com.bustransport.user.exception.DuplicateResourceException;
import com.bustransport.user.repository.DriverRepository;
import com.bustransport.user.repository.PassengerRepository;
import com.bustransport.user.repository.UserRepository;
import com.bustransport.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        // Create user based on role
        User user;
        UserRole role;

        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        switch (role) {
            case PASSENGER:
                user = createPassenger(request);
                break;
            case DRIVER:
                user = createDriver(request);
                break;
            case ADMIN:
                user = createAdmin(request);
                break;
            default:
                throw new BadRequestException("Invalid role: " + request.getRole());
        }

        // Generate tokens
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private Passenger createPassenger(RegisterRequest request) {
        Passenger passenger = Passenger.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.PASSENGER)
                .isActive(true)
                .loyaltyPoints(0)
                .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en")
                .build();

        return passengerRepository.save(passenger);
    }

    private Driver createDriver(RegisterRequest request) {
        if (request.getLicenseNumber() == null || request.getLicenseNumber().isEmpty()) {
            throw new BadRequestException("License number is required for driver registration");
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("Driver with license number " + request.getLicenseNumber() + " already exists");
        }

        Driver driver = Driver.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.DRIVER)
                .isActive(true)
                .licenseNumber(request.getLicenseNumber())
                .hireDate(LocalDate.now())
                .status(DriverStatus.AVAILABLE)
                .build();

        return driverRepository.save(driver);
    }

    private Admin createAdmin(RegisterRequest request) {
        Admin admin = Admin.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();

        return userRepository.save(admin);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public void logout(String token) {
        // In a production environment, you would add the token to a blacklist
        log.info("User logout");
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // In production: Generate reset token and send email
        log.info("Password reset requested for: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        // In production: Validate reset token and update password
        log.info("Password reset completed");
    }
}