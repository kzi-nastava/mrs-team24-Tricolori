package com.tricolori.backend.dto.vehicle;

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
