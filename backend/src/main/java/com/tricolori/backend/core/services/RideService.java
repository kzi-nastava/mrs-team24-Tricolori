package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.CancelRideExpired;
import com.tricolori.backend.core.exceptions.RideNotFoundException;
import com.tricolori.backend.infrastructure.presentation.dtos.CancelRideRequest;
import com.tricolori.backend.shared.enums.RideStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;

    @Transactional
    public void cancelRide(Long rideId, String personEmail, CancelRideRequest request) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found."));

        LocalDateTime timeNow = LocalDateTime.now();
        if (timeNow.isAfter(ride.getStartTime().minusMinutes(10))) {
            throw new CancelRideExpired("Ride cancel option expired. Ride starts within 10 minutes.");
        }

        if (ride.getDriver().getEmail().equals(personEmail)) {
            if (request.reason().isBlank()) {
                throw new IllegalArgumentException("Cancellation reason must be provided.");
            }
            ride.setStatus(RideStatus.CANCELLED_BY_DRIVER);

        } else if (ride.containsPassengerWithEmail(personEmail)) {
            ride.setStatus(RideStatus.CANCELLED_BY_PASSENGER);

        } else {
            throw new AccessDeniedException("You are not authorized to cancel this ride.");
        }

        ride.setCancellationReason(request.reason());
        rideRepository.save(ride);
    }

}
