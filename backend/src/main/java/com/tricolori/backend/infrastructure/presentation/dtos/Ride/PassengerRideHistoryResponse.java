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
    private String pickupAddress;
    private String destinationAddress;
    private LocalDateTime createdAt;
    private Double totalPrice;
    private String status;
    private Integer driverRating;
    private Integer vehicleRating;
}