package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import java.time.LocalDateTime;

import com.tricolori.backend.shared.enums.VehicleType;

public record RidePreferences(
    VehicleType vehicleType,
    boolean petFriendly,
    boolean babyFriendly,
    LocalDateTime scheduledFor
) 
{}
