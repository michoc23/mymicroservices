package com.bustransport.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "passengers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Passenger extends User {

    @Builder.Default
    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints = 0;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    public void addPoints(Integer points) {
        if (points != null && points > 0) {
            this.loyaltyPoints += points;
        }
    }

    @PrePersist
    protected void onPassengerCreate() {
        super.onCreate();
        setRole(UserRole.PASSENGER);
    }
}