package com.tricolori.backend.infrastructure.presentation.dtos.Vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLocationResponse {

    private Long vehicleId;
    private String model;
    private String plateNum;
    private Double latitude;
    private Double longitude;
    private boolean available;
}
