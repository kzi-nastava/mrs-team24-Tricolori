package com.tricolori.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerRideDetailResponse {
    private Long id;
    private String driverName;
    private String driverPhone;
    private String vehicleModel;
    private String vehicleLicensePlate;
    private String vehicleColor;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String status;
    private Double totalPrice;
    private Double distance;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer driverRating;
    private Integer vehicleRating;
    private String ratingComment;
}