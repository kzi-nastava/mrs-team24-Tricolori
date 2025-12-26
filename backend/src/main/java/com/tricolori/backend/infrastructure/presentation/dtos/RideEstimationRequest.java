package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.NotBlank;

public record RideEstimationRequest(
    @NotBlank String pickupAddress,
    @NotBlank String destinationAddress
) {}
