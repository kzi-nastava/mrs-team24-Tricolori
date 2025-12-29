package com.tricolori.backend.infrastructure.presentation.dtos;

public record VehicleDto (
    String model,
    String type,
    String plateNumber,
    int numSeats,
    boolean babyFriendly,
    boolean petFriendly
)
{}
