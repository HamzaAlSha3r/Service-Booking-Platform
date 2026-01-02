package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {

    /**
     * Find all active subscription plans
     */
    List<SubscriptionPlan> findByIsActiveTrue();

    /**
     * Find subscription plan by name
     */
    boolean existsByName(String name);
}

