package com.tricolori.backend.infrastructure.presentation.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record RideTrackingResponse(
        Long rideId,
        String status,
        VehicleLocationResponse currentLocation,
        RouteDto route,
        Integer estimatedTimeMinutes,
        LocalDateTime estimatedArrival,
        LocalDateTime scheduledFor,
        LocalDateTime startTime,
        Double price,
        DriverDto driver,
        List<PassengerDto> passengers
) {}