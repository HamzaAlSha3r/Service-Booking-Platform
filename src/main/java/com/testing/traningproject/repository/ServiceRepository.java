package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Service entity
 * Provides database access methods for Service table
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * Find all services by provider ID ordered by creation date descending
     */
    List<Service> findByProviderIdOrderByCreatedAtDesc(Long providerId);
}

