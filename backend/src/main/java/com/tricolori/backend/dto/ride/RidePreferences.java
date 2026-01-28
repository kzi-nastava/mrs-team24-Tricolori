package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;

import com.tricolori.backend.enums.VehicleType;

public record RidePreferences(
    VehicleType vehicleType,
    boolean petFriendly,
    boolean babyFriendly,
    LocalDateTime scheduledFor
) 
{}
