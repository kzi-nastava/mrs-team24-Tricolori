package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.*;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.*;
import com.tricolori.backend.shared.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.PanicRepository;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.core.exceptions.CancelRideExpiredException;
import com.tricolori.backend.core.exceptions.PersonNotFoundException;
import com.tricolori.backend.core.exceptions.RideNotFoundException;
import com.tricolori.backend.shared.enums.RideStatus;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;
    private final PersonRepository personRepository;
    private final PanicRepository panicRepository;
    private PriceList priceList;

    public List<RideHistoryResponse> getDriverHistory(
            Long driverId,
            LocalDate startDate,
            LocalDate endDate,
            String sortBy,
            String sortDirection
    ) {
        List<Ride> rides = rideRepository.findAllByDriverId(driverId);

        // Convert to DTOs
        List<RideHistoryResponse> responses = rides.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());

        // Apply sorting if different from default
        if (!"createdAt".equals(sortBy) || !"DESC".equals(sortDirection)) {
            responses = applySorting(responses, sortBy, sortDirection);
        }

        return responses;
    }

    public RideDetailResponse getDriverRideDetail(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // Verify the ride belongs to this driver
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new RuntimeException("Unauthorized access to ride");
        }

        return mapToDetailResponse(ride);
    }

    private RideHistoryResponse mapToHistoryResponse(Ride ride) {
        Passenger mainPassenger = ride.getMainPassenger();
        String passengerName = mainPassenger != null
                ? mainPassenger.getFirstName() + " " + mainPassenger.getLastName()
                : "Unknown";

        // Get addresses from route
        Address pickupAddress = ride.getRoute() != null ? ride.getRoute().getPickupAddress() : null;
        Address dropoffAddress = ride.getRoute() != null ? ride.getRoute().getDestinationAddress() : null;

        // Calculate average ratings from reviews
        Integer avgDriverRating = null;
        Integer avgVehicleRating = null;
        if (ride.getReviews() != null && !ride.getReviews().isEmpty()) {
            avgDriverRating = (int) Math.round(
                    ride.getReviews().stream()
                            .filter(r -> r.getDriverRating() != null)
                            .mapToInt(Review::getDriverRating)
                            .average()
                            .orElse(0.0)
            );
            avgVehicleRating = (int) Math.round(
                    ride.getReviews().stream()
                            .filter(r -> r.getVehicleRating() != null)
                            .mapToInt(Review::getVehicleRating)
                            .average()
                            .orElse(0.0)
            );
            // Set to null if no ratings found
            if (avgDriverRating == 0) avgDriverRating = null;
            if (avgVehicleRating == 0) avgVehicleRating = null;
        }

        // Convert duration from seconds to seconds (keep as is) or calculate if needed
        Integer duration = ride.getRoute() != null && ride.getRoute().getEstimatedTimeSeconds() != null
                ? ride.getRoute().getEstimatedTimeSeconds().intValue()
                : null;

        return RideHistoryResponse.builder()
                .id(ride.getId())
                .passengerName(passengerName)
                .pickupAddress(pickupAddress != null ? pickupAddress.getAddress() : null)
                .dropoffAddress(dropoffAddress != null ? dropoffAddress.getAddress() : null)
                .status(ride.getStatus() != null ? ride.getStatus().toString() : null)
                .totalPrice(ride.getPrice())
                .distance(ride.getRoute() != null ? ride.getRoute().getDistanceKm() : null)
                .duration(duration)
                .createdAt(ride.getCreatedAt())
                .completedAt(ride.getEndTime())
                .driverRating(avgDriverRating)
                .vehicleRating(avgVehicleRating)
                .build();
    }

    private RideDetailResponse mapToDetailResponse(Ride ride) {
        Passenger mainPassenger = ride.getMainPassenger();
        String passengerName = mainPassenger != null
                ? mainPassenger.getFirstName() + " " + mainPassenger.getLastName()
                : "Unknown";
        String passengerPhone = mainPassenger != null ? mainPassenger.getPhoneNum() : null;

        String driverName = ride.getDriver() != null
                ? ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName()
                : "Unknown";

        String vehicleModel = ride.getVehicleSpecification() != null
                ? ride.getVehicleSpecification().getModel()
                : null;
        String vehicleLicensePlate = ride.getDriver() != null && ride.getDriver().getVehicle() != null
                ? ride.getDriver().getVehicle().getPlateNum()
                : null;

        // Get addresses from route
        Address pickupAddress = ride.getRoute() != null ? ride.getRoute().getPickupAddress() : null;
        Address dropoffAddress = ride.getRoute() != null ? ride.getRoute().getDestinationAddress() : null;

        // Calculate average ratings from reviews
        Integer avgDriverRating = null;
        Integer avgVehicleRating = null;
        String ratingComment = null;
        if (ride.getReviews() != null && !ride.getReviews().isEmpty()) {
            avgDriverRating = (int) Math.round(
                    ride.getReviews().stream()
                            .filter(r -> r.getDriverRating() != null)
                            .mapToInt(Review::getDriverRating)
                            .average()
                            .orElse(0.0)
            );
            avgVehicleRating = (int) Math.round(
                    ride.getReviews().stream()
                            .filter(r -> r.getVehicleRating() != null)
                            .mapToInt(Review::getVehicleRating)
                            .average()
                            .orElse(0.0)
            );
            // Set to null if no ratings found
            if (avgDriverRating == 0) avgDriverRating = null;
            if (avgVehicleRating == 0) avgVehicleRating = null;

            // Get the first review comment as representative
            ratingComment = ride.getReviews().stream()
                    .filter(r -> r.getComment() != null && !r.getComment().isEmpty())
                    .map(Review::getComment)
                    .findFirst()
                    .orElse(null);
        }

        // Convert duration from seconds to seconds (keep as is)
        Integer duration = ride.getRoute() != null && ride.getRoute().getEstimatedTimeSeconds() != null
                ? ride.getRoute().getEstimatedTimeSeconds().intValue()
                : null;

        return RideDetailResponse.builder()
                .id(ride.getId())
                .passengerName(passengerName)
                .passengerPhone(passengerPhone)
                .driverName(driverName)
                .vehicleModel(vehicleModel)
                .vehicleLicensePlate(vehicleLicensePlate)
                .pickupAddress(pickupAddress != null ? pickupAddress.getAddress() : null)
                .pickupLatitude(pickupAddress != null ? pickupAddress.getLatitude() : null)
                .pickupLongitude(pickupAddress != null ? pickupAddress.getLongitude() : null)
                .dropoffAddress(dropoffAddress != null ? dropoffAddress.getAddress() : null)
                .dropoffLatitude(dropoffAddress != null ? dropoffAddress.getLatitude() : null)
                .dropoffLongitude(dropoffAddress != null ? dropoffAddress.getLongitude() : null)
                .status(ride.getStatus() != null ? ride.getStatus().toString() : null)
                .totalPrice(ride.getPrice())
                .distance(ride.getRoute() != null ? ride.getRoute().getDistanceKm() : null)
                .duration(duration)
                .createdAt(ride.getCreatedAt())
                .acceptedAt(ride.getScheduledFor())
                .startedAt(ride.getStartTime())
                .completedAt(ride.getEndTime())
                .driverRating(avgDriverRating)
                .vehicleRating(avgVehicleRating)
                .ratingComment(ratingComment)
                .build();
    }

    private List<RideHistoryResponse> applySorting(
            List<RideHistoryResponse> responses,
            String sortBy,
            String sortDirection
    ) {
        Comparator<RideHistoryResponse> comparator = switch (sortBy) {
            case "totalPrice" -> Comparator.comparing(
                    RideHistoryResponse::getTotalPrice,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "distance" -> Comparator.comparing(
                    RideHistoryResponse::getDistance,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "status" -> Comparator.comparing(
                    RideHistoryResponse::getStatus,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "completedAt" -> Comparator.comparing(
                    RideHistoryResponse::getCompletedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> Comparator.comparing(
                    RideHistoryResponse::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        };

        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return responses.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public StopRideResponse stopRide(Long rideId, Person driver, StopRideRequest request) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found."));

        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new AccessDeniedException("You are not authorized to stop this ride.");
        }

        // TODO: update route in some map service
        Route updatedRoute = ride.getRoute();

        ride.stop(updatedRoute);
        ride.setPrice(calculatePrice(ride));

        rideRepository.save(ride);

        return new StopRideResponse(ride.getPrice());
    }

    public Double calculatePrice(Ride ride) {
        VehicleType vehicleType =  ride.getVehicleSpecification().getType();
        return priceList.getPriceForVehicleType(vehicleType)
                + ride.getRoute().getDistanceKm() * priceList.getKmPrice();
    }

}
