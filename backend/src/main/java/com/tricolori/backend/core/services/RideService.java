package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.RideDetailResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.RideHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;

    public List<RideHistoryResponse> getDriverHistory(
            Long driverId,
            LocalDate startDate,
            LocalDate endDate,
            String sortBy,
            String sortDirection
    ) {
        List<Ride> rides = rideRepository.findDriverRideHistory(driverId, startDate, endDate);

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
        if (!ride.getDriver().getId().equals(driverId)) {
            throw new RuntimeException("Unauthorized access to ride");
        }

        return mapToDetailResponse(ride);
    }

    private RideHistoryResponse mapToHistoryResponse(Ride ride) {
        return RideHistoryResponse.builder()
                .id(ride.getId())
                .passengerName(ride.getPassenger().getFirstName() + " " + ride.getPassenger().getLastName())
                .pickupAddress(ride.getPickupAddress())
                .dropoffAddress(ride.getDropoffAddress())
                .status(ride.getStatus())
                .totalPrice(ride.getTotalPrice())
                .distance(ride.getDistance())
                .duration(ride.getDuration())
                .createdAt(ride.getCreatedAt())
                .completedAt(ride.getCompletedAt())
                .driverRating(ride.getDriverRating())
                .vehicleRating(ride.getVehicleRating())
                .build();
    }

    private RideDetailResponse mapToDetailResponse(Ride ride) {
        return RideDetailResponse.builder()
                .id(ride.getId())
                .passengerName(ride.getPassenger().getFirstName() + " " + ride.getPassenger().getLastName())
                .passengerPhone(ride.getPassenger().getPhoneNumber())
                .driverName(ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName())
                .vehicleModel(ride.getVehicle().getModel())
                .vehicleLicensePlate(ride.getVehicle().getLicensePlate())
                .pickupAddress(ride.getPickupAddress())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .dropoffAddress(ride.getDropoffAddress())
                .dropoffLatitude(ride.getDropoffLatitude())
                .dropoffLongitude(ride.getDropoffLongitude())
                .status(ride.getStatus())
                .totalPrice(ride.getTotalPrice())
                .distance(ride.getDistance())
                .duration(ride.getDuration())
                .createdAt(ride.getCreatedAt())
                .acceptedAt(ride.getAcceptedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .driverRating(ride.getDriverRating())
                .vehicleRating(ride.getVehicleRating())
                .ratingComment(ride.getRatingComment())
                .build();
    }

    private List<RideHistoryResponse> applySorting(
            List<RideHistoryResponse> responses,
            String sortBy,
            String sortDirection
    ) {
        Comparator<RideHistoryResponse> comparator = switch (sortBy) {
            case "totalPrice" -> Comparator.comparing(RideHistoryResponse::getTotalPrice);
            case "distance" -> Comparator.comparing(RideHistoryResponse::getDistance);
            case "status" -> Comparator.comparing(RideHistoryResponse::getStatus);
            case "completedAt" -> Comparator.comparing(
                    RideHistoryResponse::getCompletedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> Comparator.comparing(RideHistoryResponse::getCreatedAt);
        };

        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return responses.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}