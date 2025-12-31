package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Role;
import com.testing.traningproject.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find role by name
     * Used when assigning roles to users during registration
     */
    Optional<Role> findByName(RoleName name);
}

