package com.tricolori.backend.infrastructure.presentation.dtos;

import java.util.List;

public record VehicleLocationResponse(
        Long vehicleId,
        String model,
        String plateNum,
        Double latitude,
        Double longitude,
        boolean available,
        VehicleSpecificationDto specification
) {}