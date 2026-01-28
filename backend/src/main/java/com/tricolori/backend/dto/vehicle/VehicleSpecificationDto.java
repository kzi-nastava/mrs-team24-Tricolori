package com.tricolori.backend.dto.vehicle;

public record VehicleSpecificationDto(
        String type,
        Integer seats,
        boolean babyTransport,
        boolean petTransport
) {}