package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.request.CancelBookingRequest;
import com.testing.traningproject.model.dto.request.CreateBookingRequest;
import com.testing.traningproject.model.dto.response.BookingResponse;
import com.testing.traningproject.model.entity.*;
import com.testing.traningproject.model.enums.*;
import com.testing.traningproject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Customer Booking operations
 * Handles booking creation, cancellation, and retrieval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RefundRepository refundRepository;

    /**
     * Create a new booking (BOOKING_PAYMENT transaction)
     */
    @Transactional
    public BookingResponse createBooking(Long customerId, CreateBookingRequest request) {
        log.info("Creating booking for customer ID: {} for service ID: {}", customerId, request.getServiceId());

        // 1. Validate customer exists
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 2. Validate service exists and is active
        com.testing.traningproject.model.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getIsActive()) {
            throw new BadRequestException("Service is not active");
        }

        // 3. Validate time slot exists and is available
        TimeSlot timeSlot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));

        if (!timeSlot.getStatus().equals(TimeSlotStatus.AVAILABLE)) {
            throw new BadRequestException("Time slot is not available");
        }

        if (!timeSlot.getService().getId().equals(service.getId())) {
            throw new BadRequestException("Time slot does not belong to this service");
        }

        // 4. Check if slot date is in the future
        if (timeSlot.getSlotDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Cannot book a slot in the past");
        }

        // 5. Create booking
        Booking booking = Booking.builder()
                .customer(customer)
                .service(service)
                .slot(timeSlot)
                .totalPrice(service.getPrice())
                .status(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created with ID: {} - Status: PENDING", booking.getId());

        // 6. Process BOOKING_PAYMENT transaction (TODO: Integrate with Stripe)
        Transaction transaction = Transaction.builder()
                .user(customer)
                .booking(booking)
                .transactionType(TransactionType.BOOKING_PAYMENT)
                .amount(service.getPrice())
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.SUCCESS) // TODO: Change to PENDING then SUCCESS after Stripe
                .paymentGatewayTransactionId("MOCK_PAYMENT_" + System.currentTimeMillis()) // TODO: Real Stripe ID
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("BOOKING_PAYMENT transaction created with ID: {} - Amount: {}", transaction.getId(), transaction.getAmount());

        // 7. Update booking status to CONFIRMED (after successful payment)
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Booking ID: {} updated to CONFIRMED after successful payment", booking.getId());

        // 8. Update time slot status to BOOKED
        timeSlot.setStatus(TimeSlotStatus.BOOKED);
        timeSlot.setUpdatedAt(LocalDateTime.now());
        timeSlotRepository.save(timeSlot);
        log.info("Time slot ID: {} marked as BOOKED", timeSlot.getId());

        // TODO: Send BOOKING_CONFIRMED notification to customer
        // TODO: Send NEW_BOOKING_RECEIVED notification to provider

        return convertToBookingResponse(booking);
    }

    /**
     * Get all bookings for a customer
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long customerId) {
        log.info("Fetching all bookings for customer ID: {}", customerId);

        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        return bookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get booking by ID (with security check)
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long customerId, Long bookingId) {
        log.info("Fetching booking ID: {} for customer ID: {}", bookingId, customerId);

        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or access denied"));

        return convertToBookingResponse(booking);
    }

    /**
     * Cancel booking (create REFUND transaction)
     */
    @Transactional
    public BookingResponse cancelBooking(Long customerId, Long bookingId, CancelBookingRequest request) {
        log.info("Cancelling booking ID: {} for customer ID: {}", bookingId, customerId);

        // 1. Validate booking exists and belongs to customer
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or access denied"));

        // 2. Check if booking can be cancelled (only PENDING or CONFIRMED)
        if (!booking.getStatus().equals(BookingStatus.PENDING) && !booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            throw new BadRequestException("Booking cannot be cancelled. Current status: " + booking.getStatus());
        }

        // 3. Calculate time until booking
        LocalDateTime slotDateTime = LocalDateTime.of(booking.getSlot().getSlotDate(), booking.getSlot().getStartTime());
        Duration timeUntilBooking = Duration.between(LocalDateTime.now(), slotDateTime);
        long hoursUntilBooking = timeUntilBooking.toHours();

        // 4. Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getCancellationReason());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Booking ID: {} cancelled. Hours until booking: {}", bookingId, hoursUntilBooking);

        // 5. Free up the time slot
        TimeSlot timeSlot = booking.getSlot();
        timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
        timeSlot.setUpdatedAt(LocalDateTime.now());
        timeSlotRepository.save(timeSlot);
        log.info("Time slot ID: {} freed up and marked as AVAILABLE", timeSlot.getId());

        // 6. Determine refund amount and status
        BigDecimal refundAmount;
        RefundStatus refundStatus;

        if (hoursUntilBooking < 24) {
            // Less than 24 hours: 100% refund, auto-approve
            refundAmount = booking.getTotalPrice();
            refundStatus = RefundStatus.APPROVED;
            log.info("Auto-approving 100% refund (cancellation < 24hrs)");
        } else {
            // More than 24 hours: 50% refund, pending admin approval
            refundAmount = booking.getTotalPrice().multiply(new BigDecimal("0.50"));
            refundStatus = RefundStatus.PENDING;
            log.info("Creating refund request for 50% (cancellation > 24hrs) - Requires admin approval");
        }

        // 7. Create refund record
        Refund refund = Refund.builder()
                .booking(booking)
                .refundAmount(refundAmount)
                .refundReason(request.getCancellationReason())
                .status(refundStatus)
                .requestedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // If auto-approved, set processed date
        if (refundStatus.equals(RefundStatus.APPROVED)) {
            refund.setProcessedAt(LocalDateTime.now());
            refund.setAdminNotes("Auto-approved: Cancellation within 24 hours of booking");
        }

        refund = refundRepository.save(refund);
        log.info("Refund record created with ID: {} - Amount: {} - Status: {}", refund.getId(), refundAmount, refundStatus);

        // 8. If auto-approved, process REFUND transaction immediately
        if (refundStatus.equals(RefundStatus.APPROVED)) {
            Transaction refundTransaction = Transaction.builder()
                    .user(booking.getCustomer())
                    .booking(booking)
                    .transactionType(TransactionType.REFUND)
                    .amount(refundAmount)
                    .paymentMethod("Refund to original payment method")
                    .status(TransactionStatus.SUCCESS) // TODO: Change to PENDING then SUCCESS after Stripe
                    .paymentGatewayTransactionId("MOCK_REFUND_" + System.currentTimeMillis()) // TODO: Real Stripe ID
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(refundTransaction);
            log.info("REFUND transaction created with ID: {} - Amount: {}", refundTransaction.getId(), refundAmount);

            // Update refund with transaction ID
            refund.setTransaction(refundTransaction);
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setUpdatedAt(LocalDateTime.now());
            refundRepository.save(refund);
            log.info("Refund ID: {} marked as COMPLETED", refund.getId());

            // TODO: Send REFUND_APPROVED notification to customer
        }

        // TODO: Send BOOKING_CANCELLED notification to customer and provider

        return convertToBookingResponse(booking);
    }

    /**
     * Get all bookings for a provider (for provider dashboard)
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getProviderBookings(Long providerId) {
        log.info("Fetching all bookings for provider ID: {}", providerId);

        List<Booking> bookings = bookingRepository.findByProviderId(providerId);
        return bookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark booking as completed (create PAYOUT transaction for provider)
     */
    @Transactional
    public BookingResponse completeBooking(Long providerId, Long bookingId) {
        log.info("Completing booking ID: {} for provider ID: {}", bookingId, providerId);

        // 1. Validate booking exists and belongs to provider
        Booking booking = bookingRepository.findByIdAndProviderId(bookingId, providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or access denied"));

        // 2. Check if booking is in CONFIRMED status
        if (!booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            throw new BadRequestException("Only CONFIRMED bookings can be marked as completed. Current status: " + booking.getStatus());
        }

        // 3. Update booking status to COMPLETED
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Booking ID: {} marked as COMPLETED", bookingId);

        // 4. Create PAYOUT transaction for provider
        Transaction payout = Transaction.builder()
                .user(booking.getService().getProvider())
                .booking(booking)
                .transactionType(TransactionType.PAYOUT)
                .amount(booking.getTotalPrice()) // Full amount to provider (TODO: deduct platform commission)
                .paymentMethod("Stripe Transfer")
                .status(TransactionStatus.SUCCESS) // TODO: Change to PENDING then SUCCESS after Stripe
                .paymentGatewayTransactionId("MOCK_PAYOUT_" + System.currentTimeMillis()) // TODO: Real Stripe ID
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(payout);
        log.info("PAYOUT transaction created with ID: {} - Amount: {} for provider ID: {}",
                payout.getId(), payout.getAmount(), providerId);

        // TODO: Send BOOKING_COMPLETED notification to customer
        // TODO: Send PAYOUT_PROCESSED notification to provider

        return convertToBookingResponse(booking);
    }

    /**
     * Convert Booking entity to BookingResponse DTO
     */
    private BookingResponse convertToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName())
                .serviceId(booking.getService().getId())
                .serviceTitle(booking.getService().getTitle())
                .providerName(booking.getService().getProvider().getFirstName() + " " +
                        booking.getService().getProvider().getLastName())
                .slotDate(booking.getSlot().getSlotDate())
                .startTime(booking.getSlot().getStartTime())
                .endTime(booking.getSlot().getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .cancellationReason(booking.getCancellationReason())
                .bookingDate(booking.getBookingDate())
                .cancelledAt(booking.getCancelledAt())
                .completedAt(booking.getCompletedAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

