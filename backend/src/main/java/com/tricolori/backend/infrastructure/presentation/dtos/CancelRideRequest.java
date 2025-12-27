package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.NotBlank;

public record CancelRideRequest(
        @NotBlank String reason
) {}
