package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import com.tricolori.backend.shared.enums.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateRideRequest {

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // Intermediate stops (optional)
//    private List<IntermediateStopRequest> intermediateStops;

    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;

    private LocalDateTime scheduledFor;
}