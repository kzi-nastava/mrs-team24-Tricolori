package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import com.tricolori.backend.infrastructure.presentation.dtos.DriverDto;
import com.tricolori.backend.infrastructure.presentation.dtos.PassengerDto;
import com.tricolori.backend.infrastructure.presentation.dtos.RouteDto;
import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleLocationResponse;

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