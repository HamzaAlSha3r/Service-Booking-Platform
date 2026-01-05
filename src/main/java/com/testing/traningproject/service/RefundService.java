package com.testing.traningproject.service;

import com.testing.traningproject.mapper.RefundMapper;
import com.testing.traningproject.model.dto.response.RefundResponse;
import com.testing.traningproject.model.entity.Refund;
import com.testing.traningproject.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Refund operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;

    /**
     * Get all refunds for a specific customer
     * @param customerId Customer ID
     * @return List of refund responses
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getCustomerRefunds(Long customerId) {
        log.info("Fetching refunds for customer ID: {}", customerId);

        List<Refund> refunds = refundRepository.findByBookingCustomerId(customerId);

        log.info("Found {} refunds for customer ID: {}", refunds.size(), customerId);

        return refundMapper.toResponseList(refunds);
    }
}

