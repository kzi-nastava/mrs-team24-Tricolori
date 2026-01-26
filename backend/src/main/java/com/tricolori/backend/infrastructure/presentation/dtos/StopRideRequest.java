package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record StopRideRequest(
        @NotNull @Valid Location location
) {}
