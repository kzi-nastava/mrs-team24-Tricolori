package com.tricolori.backend.dto.ride;

import com.tricolori.backend.enums.RideStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideAssignmentResponse {

    private Long id;
    private Double price;
    private RideStatus status;

    private String passengerFirstName;
    private String passengerLastName;
    private String passengerEmail;
    private String passengerPhoneNum;

    private String driverFirstName;
    private String driverLastName;
    private String driverEmail;
    private String driverPhoneNum;

    private String vehiclePlateNum;
    private String vehicleModel;

    private String routeGeometry;
    private Double distanceKm;
    private Long estimatedTimeSeconds;
    private String pickupAddress;
    private String destinationAddress;
}