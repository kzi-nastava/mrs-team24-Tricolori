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
public class RideHistoryResponse {
    private Long id;
    private String passengerName;
    private String pickupAddress;
    private String dropoffAddress;
    private String status;
    private Double totalPrice;
    private Double distance;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer driverRating;
    private Integer vehicleRating;
}