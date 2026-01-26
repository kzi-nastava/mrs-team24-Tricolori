package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleLocationResponse;

import java.time.LocalDateTime;
import java.util.List;

public record RideStatusResponse(
        Long rideId,
        String status,
        LocalDateTime scheduledFor,
        LocalDateTime startTime,
        LocalDateTime estimatedEndTime,
        VehicleLocationResponse currentLocation,
        RouteDto route,
        DriverDto driver,
        List<PassengerDto> passengers,
        Double price
) {}