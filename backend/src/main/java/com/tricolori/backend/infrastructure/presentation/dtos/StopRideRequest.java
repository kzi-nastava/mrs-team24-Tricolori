package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record StopRideRequest(
        @NotNull @Valid LocationDto location
) {}
