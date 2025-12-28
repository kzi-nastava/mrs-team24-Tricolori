package com.tricolori.backend.infrastructure.presentation.dtos;

public record VehicleSpecificationDto(
        String type,
        Integer seats,
        boolean babyTransport,
        boolean petTransport
) {}