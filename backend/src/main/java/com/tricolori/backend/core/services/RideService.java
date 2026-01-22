package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Panic;
import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.PanicRepository;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.CancelRideExpiredException;
import com.tricolori.backend.core.exceptions.PersonNotFoundException;
import com.tricolori.backend.core.exceptions.RideNotFoundException;
import com.tricolori.backend.infrastructure.presentation.dtos.CancelRideRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.PanicRideRequest;
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
    private final PersonRepository personRepository;
    private final PanicRepository panicRepository;

    @Transactional
    public void cancelRide(Long rideId, String personEmail, CancelRideRequest request) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found."));

        if (ride.getDriver().getEmail().equals(personEmail)) {
            if (request.reason().isBlank()) {
                throw new IllegalArgumentException("Cancellation reason must be provided.");
            }
            ride.setStatus(RideStatus.CANCELLED_BY_DRIVER);

        } else if (ride.containsPassengerWithEmail(personEmail)) {
            LocalDateTime timeNow = LocalDateTime.now();
            if (timeNow.isAfter(ride.getStartTime().minusMinutes(10))) {
                throw new CancelRideExpiredException("Ride cancel option expired. Ride starts within 10 minutes.");
            }
            ride.setStatus(RideStatus.CANCELLED_BY_PASSENGER);

        } else {
            throw new AccessDeniedException("You are not authorized to cancel this ride.");
        }

        ride.setCancellationReason(request.reason());
        rideRepository.save(ride);
    }

    @Transactional
    public void panicRide(Long rideId, String personEmail, PanicRideRequest request) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found."));

        boolean isDriver = ride.getDriver().getEmail().equals(personEmail);
        boolean isPassenger = ride.containsPassengerWithEmail(personEmail);

        if (!isDriver && !isPassenger) {
            throw new AccessDeniedException("You are not part of this ride.");
        }

        Person person = personRepository.findByEmail(personEmail)
                .orElseThrow(() -> new PersonNotFoundException("Person not found."));

        ride.setStatus(RideStatus.PANIC);
        ride.setEndTime(LocalDateTime.now());

        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setPerson(person);
        panic.setVehicleLocation(request.vehicleLocation());

        panicRepository.save(panic);
        rideRepository.save(ride);
    }

}
