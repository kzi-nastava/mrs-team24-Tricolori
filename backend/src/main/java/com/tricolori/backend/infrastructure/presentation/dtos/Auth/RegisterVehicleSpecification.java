package com.tricolori.backend.infrastructure.presentation.dtos.Auth;

import com.tricolori.backend.shared.enums.VehicleType;

public record RegisterVehicleSpecification(
    String model,
    VehicleType type,
    Integer seatNumber,
    Boolean petFriendly,
    Boolean babyFriendly
)
{}
