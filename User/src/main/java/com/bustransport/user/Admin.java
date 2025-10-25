package com.bustransport.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @Column(length = 100)
    private String department;

    @Column(name = "permission_level")
    private Integer permissionLevel;

    @PrePersist
    protected void onAdminCreate() {
        super.onCreate();
        setRole(UserRole.ADMIN);
    }
}