package com.tricolori.backend.dto.ride;

import jakarta.validation.constraints.NotBlank;

public record RideEstimationRequest(
    @NotBlank String pickupAddress,
    @NotBlank String destinationAddress
) {}
