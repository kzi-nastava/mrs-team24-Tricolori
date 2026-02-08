package com.tricolori.backend.dto.ride;

import com.tricolori.backend.enums.VehicleType;
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