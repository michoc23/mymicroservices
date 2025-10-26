package com.bustransport.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "drivers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Driver extends User {

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "bus_id")
    private Long busId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.AVAILABLE;

    public void startShift() {
        this.status = DriverStatus.ON_DUTY;
    }

    public void endShift() {
        this.status = DriverStatus.OFF_DUTY;
    }

    @PrePersist
    protected void onDriverCreate() {
        super.onCreate();
        setRole(UserRole.DRIVER);
    }
}