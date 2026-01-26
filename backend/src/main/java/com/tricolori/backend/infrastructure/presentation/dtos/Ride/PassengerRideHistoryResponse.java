package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerRideHistoryResponse {
    private Long id;
    private String driverName;
    private String vehicleModel;
    private String pickupAddress;
    private String destinationAddress;
    private String status;
    private Double totalPrice;
    private Double distance;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer driverRating;
    private Integer vehicleRating;
}

