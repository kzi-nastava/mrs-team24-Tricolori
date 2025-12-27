package com.tricolori.backend.infrastructure.presentation.dtos;

public record ProfileResponse(
    String email,
    String firstName,
    String lastName,
    String homeAddress,
    String phoneNumber,
    String pfp,
    
    VehicleDto vehicle, // null for non-drivers
    Double activeHours  // null for non-drivers
)
{}
