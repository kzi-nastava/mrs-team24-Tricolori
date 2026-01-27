package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.*;
import com.tricolori.backend.core.domain.repositories.ReviewRepository;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.RideNotFoundException;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.RideRatingRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.RideRatingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RideRepository rideRepository;

    // ================= rate ride =================

    @Transactional
    public void rateRide(
            Long rideId,
            Long passengerId,
            RideRatingRequest request
    ) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        Passenger passenger = ride.getPassengers().stream()
                .filter(p -> p.getId().equals(passengerId))
                .findFirst()
                .orElseThrow(() ->
                        new AccessDeniedException("not a passenger of this ride")
                );

        if (!ride.canBeReviewedBy(passenger)) {
            throw new IllegalStateException("ride cannot be reviewed");
        }

        if (reviewRepository.existsByRideIdAndReviewerId(rideId, passengerId)) {
            throw new IllegalStateException("ride already reviewed");
        }

        Review review = new Review();
        review.setRide(ride);
        review.setReviewer(passenger);
        review.setDriverRating(request.getDriverRating());
        review.setVehicleRating(request.getVehicleRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);
    }

    // ================= rating status =================

    @Transactional(readOnly = true)
    public RideRatingResponse getRatingStatus(
            Long rideId,
            Long passengerId
    ) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        Passenger passenger = ride.getPassengers().stream()
                .filter(p -> p.getId().equals(passengerId))
                .findFirst()
                .orElseThrow(() ->
                        new AccessDeniedException("not a passenger of this ride")
                );

        boolean alreadyRated =
                reviewRepository.existsByRideIdAndReviewerId(rideId, passengerId);

        boolean canRate =
                !alreadyRated && ride.canBeReviewedBy(passenger);

        LocalDateTime deadline = ride.getEndTime().plusDays(3);

        boolean deadlinePassed = deadline.isBefore(LocalDateTime.now());

        return new RideRatingResponse(canRate, alreadyRated, deadlinePassed, deadline);
    }

    // ================= aggregates (optional) =================

    @Transactional(readOnly = true)
    public Double getAverageDriverRating(Long rideId) {
        return reviewRepository.getAverageDriverRating(rideId);
    }

    @Transactional(readOnly = true)
    public Double getAverageVehicleRating(Long rideId) {
        return reviewRepository.getAverageVehicleRating(rideId);
    }
}
