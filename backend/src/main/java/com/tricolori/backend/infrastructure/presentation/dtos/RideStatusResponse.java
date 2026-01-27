package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleLocationResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideStatusResponse {

    private Long rideId;
    private String status;
    private LocalDateTime scheduledFor;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
    private VehicleLocationResponse currentLocation;
    private RouteDto route;
    private DriverDto driver;
    private List<PassengerDto> passengers;
    private Double price;
}
