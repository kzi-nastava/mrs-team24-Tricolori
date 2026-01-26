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
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.PassengerRideDetailResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.PassengerRideHistoryResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.RideDetailResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.RideHistoryResponse;
import com.tricolori.backend.infrastructure.presentation.mappers.RideMapper;
import com.tricolori.backend.infrastructure.presentation.dtos.*;
import com.tricolori.backend.shared.enums.RideStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;
    private final PersonRepository personRepository;
    private final PanicRepository panicRepository;
    private final RideMapper rideMapper;

    // ==================== DRIVER HISTORY ====================

    public List<RideHistoryResponse> getDriverHistory(
            Long driverId,
            LocalDate startDate,
            LocalDate endDate,
            String sortBy,
            String sortDirection
    ) {
        List<Ride> rides = rideRepository.findAllByDriverId(driverId);

        // Apply date filtering
        List<Ride> filteredRides = applyDateFilter(rides, startDate, endDate);

        // Convert to DTOs using MapStruct
        List<RideHistoryResponse> responses = filteredRides.stream()
                .map(rideMapper::toDriverHistoryResponse)
                .collect(Collectors.toList());

        // Apply sorting
        return applySortingToDriverHistory(responses, sortBy, sortDirection);
    }

    public RideDetailResponse getDriverRideDetail(Long rideId, Long driverId) {
        Ride ride = findRideById(rideId);
        validateDriverAccess(ride, driverId);
        return rideMapper.toDriverDetailResponse(ride);
    }

    // ==================== PASSENGER HISTORY ====================

    public List<PassengerRideHistoryResponse> getPassengerHistory(
            Long passengerId,
            LocalDate startDate,
            LocalDate endDate,
            String sortBy,
            String sortDirection
    ) {
        List<Ride> rides = rideRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);

        // Apply date filtering
        List<Ride> filteredRides = applyDateFilter(rides, startDate, endDate);

        // Convert to DTOs using MapStruct
        List<PassengerRideHistoryResponse> responses = filteredRides.stream()
                .map(rideMapper::toPassengerHistoryResponse)
                .collect(Collectors.toList());

        // Apply sorting
        return applySortingToPassengerHistory(responses, sortBy, sortDirection);
    }

    public PassengerRideDetailResponse getPassengerRideDetail(Long rideId, Long passengerId) {
        Ride ride = findRideById(rideId);
        validatePassengerAccess(ride, passengerId);
        return rideMapper.toPassengerDetailResponse(ride);
    }

    // ==================== RIDE ACTIONS ====================

    @Transactional
    public void cancelRide(Long rideId, String personEmail, CancelRideRequest request) {
        Ride ride = findRideById(rideId);

        if (isDriver(ride, personEmail)) {
            handleDriverCancellation(ride, request);
        } else if (isPassenger(ride, personEmail)) {
            handlePassengerCancellation(ride, request);
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

    // ==================== HELPER METHODS ====================

    private Ride findRideById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found."));
    }

    private void validateDriverAccess(Ride ride, Long driverId) {
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("Unauthorized access to ride");
        }
    }

    private void validatePassengerAccess(Ride ride, Long passengerId) {
        boolean isPassenger = ride.getPassengers().stream()
                .anyMatch(p -> p.getId().equals(passengerId));

        if (!isPassenger) {
            throw new AccessDeniedException("Unauthorized access to ride");
        }
    }

    private boolean isDriver(Ride ride, String email) {
        return ride.getDriver().getEmail().equals(email);
    }

    private boolean isPassenger(Ride ride, String email) {
        return ride.containsPassengerWithEmail(email);
    }

    private void handleDriverCancellation(Ride ride, CancelRideRequest request) {
        if (request.reason().isBlank()) {
            throw new IllegalArgumentException("Cancellation reason must be provided.");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_DRIVER);
    }

    private void handlePassengerCancellation(Ride ride, CancelRideRequest request) {
        LocalDateTime timeNow = LocalDateTime.now();
        if (timeNow.isAfter(ride.getStartTime().minusMinutes(10))) {
            throw new CancelRideExpiredException("Ride cancel option expired. Ride starts within 10 minutes.");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_PASSENGER);
    }

    private List<Ride> applyDateFilter(List<Ride> rides, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return rides;
        }

        return rides.stream()
                .filter(ride -> {
                    LocalDate rideDate = ride.getCreatedAt().toLocalDate();
                    boolean afterStart = startDate == null || !rideDate.isBefore(startDate);
                    boolean beforeEnd = endDate == null || !rideDate.isAfter(endDate);
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
    }

    private List<RideHistoryResponse> applySortingToDriverHistory(
            List<RideHistoryResponse> responses,
            String sortBy,
            String sortDirection
    ) {
        if ("createdAt".equals(sortBy) && "DESC".equals(sortDirection)) {
            return responses; // Already sorted by default
        }

        Comparator<RideHistoryResponse> comparator = getDriverHistoryComparator(sortBy);

        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return responses.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Comparator<RideHistoryResponse> getDriverHistoryComparator(String sortBy) {
        return switch (sortBy) {
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
    }

    private List<PassengerRideHistoryResponse> applySortingToPassengerHistory(
            List<PassengerRideHistoryResponse> responses,
            String sortBy,
            String sortDirection
    ) {
        if ("createdAt".equals(sortBy) && "DESC".equals(sortDirection)) {
            return responses; // Already sorted by default
        }

        Comparator<PassengerRideHistoryResponse> comparator = getPassengerHistoryComparator(sortBy);

        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return responses.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Comparator<PassengerRideHistoryResponse> getPassengerHistoryComparator(String sortBy) {
        return switch (sortBy) {
            case "totalPrice" -> Comparator.comparing(
                    PassengerRideHistoryResponse::getTotalPrice,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "distance" -> Comparator.comparing(
                    PassengerRideHistoryResponse::getDistance,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "status" -> Comparator.comparing(
                    PassengerRideHistoryResponse::getStatus,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "completedAt" -> Comparator.comparing(
                    PassengerRideHistoryResponse::getCompletedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> Comparator.comparing(
                    PassengerRideHistoryResponse::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        };
    }
}