package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.ProviderAvailability;
import com.testing.traningproject.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, Long> {

    /**
     * Find all availability for a specific provider ordered by day
     */
    List<ProviderAvailability> findByProviderIdOrderByDayOfWeekAsc(Long providerId);

    /**
     * Find availability by provider and specific day
     */
    List<ProviderAvailability> findByProviderIdAndDayOfWeek(Long providerId, DayOfWeek dayOfWeek);
}

