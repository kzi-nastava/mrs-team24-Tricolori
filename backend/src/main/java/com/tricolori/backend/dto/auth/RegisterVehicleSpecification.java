package com.tricolori.backend.dto.auth;

import com.tricolori.backend.enums.VehicleType;

public record RegisterVehicleSpecification(
    String model,
    VehicleType type,
    Integer seatNumber,
    Boolean petFriendly,
    Boolean babyFriendly
)
{}
