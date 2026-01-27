package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import com.tricolori.backend.infrastructure.presentation.dtos.StopDto;
import com.tricolori.backend.shared.enums.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateRideRequest {

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private List<StopDto> stops;

    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;

    private LocalDateTime scheduledFor;
}