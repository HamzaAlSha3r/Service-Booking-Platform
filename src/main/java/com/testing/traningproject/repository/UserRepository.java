package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity
 * Provides database access methods for User table
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * Used for login and checking if email already exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * Used for validation during registration
     */
    boolean existsByEmail(String email);
}
