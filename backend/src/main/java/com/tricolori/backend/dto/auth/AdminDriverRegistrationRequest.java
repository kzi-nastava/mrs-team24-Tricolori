package com.tricolori.backend.dto.auth;

import com.tricolori.backend.enums.VehicleType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminDriverRegistrationRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String phone,
    @NotBlank String address,
    @NotBlank @Email String email,
    @NotBlank String vehicleModel,
    @NotNull VehicleType vehicleType,
    @NotBlank String registrationPlate,
    @NotNull @Min(1) Integer seatNumber,
    @NotNull Boolean petFriendly,
    @NotNull Boolean babyFriendly
) {}
