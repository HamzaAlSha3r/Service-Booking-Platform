package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Service entity
 * Provides database access methods for Service table
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
}

