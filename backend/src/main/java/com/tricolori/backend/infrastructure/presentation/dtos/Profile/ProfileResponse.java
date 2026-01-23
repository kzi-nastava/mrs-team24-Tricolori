package com.tricolori.backend.infrastructure.presentation.dtos.Profile;

import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse
{
    private String email;
    private String firstName;
    private String lastName;
    private String homeAddress;
    private String phoneNumber;
    private String pfp;
    private VehicleDto vehicle; // null for non-drivers
    private Double activeHours;  // null for non-drivers
}
