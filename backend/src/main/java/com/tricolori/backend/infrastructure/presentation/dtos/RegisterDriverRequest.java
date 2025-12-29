package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDriverRequest(
    @NotBlank @Email String email,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String homeAddress,
    @NotBlank String phoneNum,
    @NotBlank String vehicleModel,
    @NotBlank String vehicleType,
    @NotBlank String plateNumber,
    @NotBlank int numSeats,
    @NotBlank boolean babyFriendly,
    @NotBlank boolean petFriendly)
{}
