package com.bustransport.user.repository;

import com.bustransport.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findByRole(String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = :isActive")
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(:email IS NULL OR u.email LIKE %:email%) AND " +
            "(:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(String email, String role, Pageable pageable);
}