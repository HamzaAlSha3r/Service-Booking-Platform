package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.request.CreateReviewRequest;
import com.testing.traningproject.model.dto.response.ReviewResponse;
import com.testing.traningproject.model.entity.Booking;
import com.testing.traningproject.model.entity.Review;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.model.enums.BookingStatus;
import com.testing.traningproject.repository.BookingRepository;
import com.testing.traningproject.repository.ReviewRepository;
import com.testing.traningproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Review operations
 * Handles review creation and retrieval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Create a new review (only for COMPLETED bookings)
     */
    @Transactional
    public ReviewResponse createReview(Long customerId, CreateReviewRequest request) {
        log.info("Creating review for booking ID: {} by customer ID: {}", request.getBookingId(), customerId);

        // 1. Validate booking exists and belongs to customer
        Booking booking = bookingRepository.findByIdAndCustomerId(request.getBookingId(), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or access denied"));

        // 2. Check if booking is COMPLETED
        if (!booking.getStatus().equals(BookingStatus.COMPLETED)) {
            throw new BadRequestException("Only COMPLETED bookings can be reviewed. Current status: " + booking.getStatus());
        }

        // 3. Check if review already exists for this booking
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("Review already exists for this booking");
        }

        // 4. Create review
        Review review = Review.builder()
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);
        log.info("Review created with ID: {} - Rating: {}/5", review.getId(), review.getRating());

        // 5. Update provider's denormalized fields (average_rating, total_reviews)
        // NOTE: These fields should be added to User entity in future
        User provider = booking.getService().getProvider();

        // Calculate new average rating
        Double newAverageRating = reviewRepository.calculateAverageRatingByProviderId(provider.getId());
        Long totalReviews = reviewRepository.countByProviderId(provider.getId());

        log.info("Provider ID: {} - New average rating: {} - Total reviews: {}",
                provider.getId(), newAverageRating, totalReviews);

        // Note: averageRating and totalReviews can be calculated dynamically using repository methods
        // No need to store them in User entity (denormalization trade-off)

        // Send REVIEW_RECEIVED notification to provider
        notificationService.createNotification(
            provider,
            com.testing.traningproject.model.enums.NotificationType.REVIEW_RECEIVED,
            "New Review Received ⭐",
            "You received a new " + request.getRating() + "-star review for '" +
            booking.getService().getTitle() + "' from " +
            booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName() +
            (request.getComment() != null ? "\n\nComment: \"" + request.getComment() + "\"" : "")
        );

        return convertToReviewResponse(review);
    }

    /**
     * Get all reviews for a provider
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getProviderReviews(Long providerId) {
        log.info("Fetching all reviews for provider ID: {}", providerId);

        // Validate provider exists
        userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        List<Review> reviews = reviewRepository.findByProviderId(providerId);
        return reviews.stream()
                .map(this::convertToReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all reviews for a service
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getServiceReviews(Long serviceId) {
        log.info("Fetching all reviews for service ID: {}", serviceId);

        List<Review> reviews = reviewRepository.findByServiceId(serviceId);
        return reviews.stream()
                .map(this::convertToReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Review entity to ReviewResponse DTO
     */
    private ReviewResponse convertToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .serviceTitle(review.getBooking().getService().getTitle())
                .customerName(review.getBooking().getCustomer().getFirstName() + " " +
                        review.getBooking().getCustomer().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

