package com.tricolori.backend.dto.ride;

import com.tricolori.backend.dto.profile.DriverDto;
import com.tricolori.backend.dto.profile.PassengerDto;
import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;

import java.time.LocalDateTime;
import java.util.List;

public record RideTrackingResponse(
        Long rideId,
        String status,
        VehicleLocationResponse currentLocation,
        DetailedRouteResponse route,
        Integer estimatedTimeMinutes,
        LocalDateTime estimatedArrival,
        LocalDateTime scheduledFor,
        LocalDateTime startTime,
        Double price,
        DriverDto driver,
        List<PassengerDto> passengers
) {}