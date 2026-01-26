package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.*;
import com.tricolori.backend.core.domain.repositories.PanicRepository;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.CancelRideExpiredException;
import com.tricolori.backend.core.exceptions.PersonNotFoundException;
import com.tricolori.backend.core.exceptions.RideNotFoundException;
import com.tricolori.backend.infrastructure.presentation.dtos.*;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.*;
import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleLocationResponse;
import com.tricolori.backend.infrastructure.presentation.mappers.RideMapper;
import com.tricolori.backend.shared.enums.RideStatus;
import com.tricolori.backend.shared.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;
    private final PersonRepository personRepository;
    private final PanicRepository panicRepository;
    private final RideMapper rideMapper;
    private final ReviewService reviewService;
    private final PriceListService priceListService;
    private final InconsistencyReportService inconsistencyReportService;

    // ================= driver =================

    public Page<RideHistoryResponse> getDriverHistory(
            Long driverId,
            Pageable pageable
    ) {
        return rideRepository
                .findAllDriverRides(driverId, pageable)
                .map(rideMapper::toDriverHistoryResponse);
    }

    public RideDetailResponse getDriverRideDetail(
            Long rideId,
            Long driverId
    ) {
        Ride ride = getRideOrThrow(rideId);
        validateDriverAccess(ride, driverId);

        RideDetailResponse response =
                rideMapper.toDriverDetailResponse(ride);

        response.setDriverRating(
                round(reviewService.getAverageDriverRating(rideId))
        );
        response.setVehicleRating(
                round(reviewService.getAverageVehicleRating(rideId))
        );

        return response;
    }

    @Transactional
    public void completeRide(Long rideId, Long driverId) {
        Ride ride = getRideOrThrow(rideId);

        // security check
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("not authorized to complete this ride");
        }

        // state validation (optional but recommended)
        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new IllegalStateException("ride is not in progress");
        }

        // complete ride
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());

        // calculate final price
        ride.setPrice(calculatePrice(ride));

        rideRepository.save(ride);
    }

    // ================= passenger =================

    public Page<PassengerRideHistoryResponse> getPassengerHistory(
            Long passengerId,
            Pageable pageable
    ) {
        return rideRepository
                .findAllPassengerRides(passengerId, pageable)
                .map(rideMapper::toPassengerHistoryResponse);
    }

    public PassengerRideDetailResponse getPassengerRideDetail(
            Long rideId,
            Long passengerId
    ) {
        Ride ride = getRideOrThrow(rideId);
        validatePassengerAccess(ride, passengerId);

        PassengerRideDetailResponse response =
                rideMapper.toPassengerDetailResponse(ride);

        response.setDriverRating(
                round(reviewService.getAverageDriverRating(rideId))
        );
        response.setVehicleRating(
                round(reviewService.getAverageVehicleRating(rideId))
        );

        return response;
    }

    public RideTrackingResponse trackRide(Long rideId) {
        Ride ride = getRideOrThrow(rideId);

        Integer estimatedMinutes =
                ride.getRoute() != null && ride.getRoute().getEstimatedTimeSeconds() != null
                        ? (int) Math.round(ride.getRoute().getEstimatedTimeSeconds() / 60.0)
                        : null;

        LocalDateTime estimatedArrival =
                ride.getStartTime() != null && estimatedMinutes != null
                        ? ride.getStartTime().plusMinutes(estimatedMinutes)
                        : null;

        VehicleLocationResponse currentLocation =
                ride.getDriver() != null && ride.getDriver().getVehicle().getLocation() != null
                        ? new VehicleLocationResponse(
                        ride.getDriver().getVehicle().getId(),
                        ride.getDriver().getVehicle().getPlateNum(),
                        ride.getDriver().getVehicle().getLocation().getLatitude(),
                        ride.getDriver().getVehicle().getLocation().getLongitude(),
                        ride.getDriver().getVehicle().isAvailable()
                )
                        : null;

        return new RideTrackingResponse(
                ride.getId(),
                ride.getStatus().name(),
                currentLocation,
                null,               // route dto (no mapper yet)
                estimatedMinutes,
                estimatedArrival,
                ride.getScheduledFor(),
                ride.getStartTime(),
                ride.getPrice(),
                null,               // driver dto (no mapper yet)
                null                // passenger dto list (no mapper yet)
        );
    }

    // ================= admin ========================
    public RideStatusResponse getRideStatus(Long rideId) {
        Ride ride = getRideOrThrow(rideId);
        return new RideStatusResponse(ride.getId(), ride.getStatus().name(), ride.getScheduledFor(), ride.getStartTime(), ride.getEndTime(), null, null, null, null, ride.getPrice());
    }

    public RideStatusResponse getCurrentRideByDriver(Long driverId) {
        Ride ride = rideRepository.findCurrentRideByDriver(driverId)
                .orElseThrow(() ->
                        new RideNotFoundException("no active ride for this driver")
                );

        return new RideStatusResponse(ride.getId(), ride.getStatus().name(), ride.getScheduledFor(), ride.getStartTime(), ride.getEndTime(), null, null, null, null, ride.getPrice());
    }




    // ================= ride actions =================

    @Transactional
    public void cancelRide(
            Long rideId,
            String personEmail,
            CancelRideRequest request
    ) {
        Ride ride = getRideOrThrow(rideId);

        if (isDriver(ride, personEmail)) {
            cancelByDriver(ride, request);
        } else if (isPassenger(ride, personEmail)) {
            cancelByPassenger(ride, request);
        } else {
            throw new AccessDeniedException("not authorized");
        }

        ride.setCancellationReason(request.reason());
        rideRepository.save(ride);
    }

    @Transactional
    public void panicRide(
            Long rideId,
            String personEmail,
            PanicRideRequest request
    ) {
        Ride ride = getRideOrThrow(rideId);

        boolean isParticipant =
                ride.getDriver().getEmail().equals(personEmail)
                        || ride.containsPassengerWithEmail(personEmail);

        if (!isParticipant) {
            throw new AccessDeniedException("not part of this ride");
        }

        Person person = personRepository.findByEmail(personEmail)
                .orElseThrow(() -> new PersonNotFoundException("person not found"));

        ride.setStatus(RideStatus.PANIC);
        ride.setEndTime(LocalDateTime.now());

        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setPerson(person);
        panic.setVehicleLocation(request.vehicleLocation());

        panicRepository.save(panic);
        rideRepository.save(ride);
    }

    @Transactional
    public StopRideResponse stopRide(
            Long rideId,
            Person driver,
            StopRideRequest request
    ) {
        Ride ride = getRideOrThrow(rideId);

        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new AccessDeniedException("not authorized");
        }

        Route updatedRoute = ride.getRoute(); // placeholder
        ride.stop(updatedRoute);
        ride.setPrice(calculatePrice(ride));

        rideRepository.save(ride);
        return new StopRideResponse(ride.getPrice());
    }

    // ================= helpers =================

    private Ride getRideOrThrow(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));
    }

    private void validateDriverAccess(Ride ride, Long driverId) {
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("unauthorized");
        }
    }

    private void validatePassengerAccess(Ride ride, Long passengerId) {
        boolean isPassenger = ride.getPassengers().stream()
                .anyMatch(p -> p.getId().equals(passengerId));

        if (!isPassenger) {
            throw new AccessDeniedException("unauthorized");
        }
    }

    private boolean isDriver(Ride ride, String email) {
        return ride.getDriver().getEmail().equals(email);
    }

    private boolean isPassenger(Ride ride, String email) {
        return ride.containsPassengerWithEmail(email);
    }

    private void cancelByDriver(Ride ride, CancelRideRequest request) {
        if (request.reason().isBlank()) {
            throw new IllegalArgumentException("reason required");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_DRIVER);
    }

    private void cancelByPassenger(Ride ride, CancelRideRequest request) {
        if (LocalDateTime.now().isAfter(ride.getStartTime().minusMinutes(10))) {
            throw new CancelRideExpiredException("cancel expired");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_PASSENGER);
    }

    private Double calculatePrice(Ride ride) {
        VehicleType type = ride.getVehicleSpecification().getType();

        Double basePrice =
                priceListService.calculateBasePrice(type);

        Double kmPrice =
                priceListService.getKmPrice();

        return basePrice + ride.getRoute().getDistanceKm() * kmPrice;
    }

    private Integer round(Double value) {
        return value != null ? (int) Math.round(value) : null;
    }
}
