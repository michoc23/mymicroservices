package com.bustransport.user.service;

import com.bustransport.user.dto.DriverResponse;
import com.bustransport.user.entity.Driver;
import com.bustransport.user.entity.DriverStatus;
import com.bustransport.user.exception.ResourceNotFoundException;
import com.bustransport.user.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverResponse getDriverByUserId(Long userId) {
        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + userId));

        return mapToDriverResponse(driver);
    }

    @Transactional
    public void startShift(Long userId) {
        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + userId));

        driver.startShift();
        driverRepository.save(driver);

        log.info("Driver started shift: {}", driver.getEmail());
    }

    @Transactional
    public void endShift(Long userId) {
        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + userId));

        driver.endShift();
        driverRepository.save(driver);

        log.info("Driver ended shift: {}", driver.getEmail());
    }

    public String getDriverStatus(Long userId) {
        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + userId));

        return driver.getStatus().name();
    }

    @Transactional
    public void assignBus(Long userId, Long busId) {
        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + userId));

        driver.setBusId(busId);
        driverRepository.save(driver);

        log.info("Bus {} assigned to driver: {}", busId, driver.getEmail());
    }

    @Transactional
    public DriverResponse updateDriver(Long userId, UpdateDriverRequest request) {
        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + userId));

        if (request.getLicenseNumber() != null) {
            driver.setLicenseNumber(request.getLicenseNumber());
        }

        Driver updatedDriver = driverRepository.save(driver);
        log.info("Driver profile updated: {}", updatedDriver.getEmail());

        return mapToDriverResponse(updatedDriver);
    }

    private DriverResponse mapToDriverResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setFirstName(driver.getFirstName());
        response.setLastName(driver.getLastName());
        response.setEmail(driver.getEmail());
        response.setPhoneNumber(driver.getPhoneNumber());
        response.setRole(driver.getRole().name());
        response.setIsActive(driver.getIsActive());
        response.setLastLoginAt(driver.getLastLoginAt());
        response.setCreatedAt(driver.getCreatedAt());
        response.setUpdatedAt(driver.getUpdatedAt());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setHireDate(driver.getHireDate());
        response.setBusId(driver.getBusId());
        response.setStatus(driver.getStatus().name());
        return response;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateDriverRequest {
        private String licenseNumber;
    }
}