package com.tricolori.backend.infrastructure.presentation.dtos.Vehicle;

import com.tricolori.backend.infrastructure.presentation.dtos.VehicleSpecificationDto;

import java.util.List;

public record VehicleLocationResponse(
        Long vehicleId,
        String plateNum,
        Double latitude,
        Double longitude,
        boolean available
) {}