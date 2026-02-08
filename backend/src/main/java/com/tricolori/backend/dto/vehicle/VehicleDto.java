package com.tricolori.backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {
    private String model;
    private String type;
    private String plateNumber;
    private int numSeats;
    private boolean babyFriendly;
    private boolean petFriendly;
}
