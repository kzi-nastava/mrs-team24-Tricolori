package com.tricolori.backend.dto.ride;

import com.tricolori.backend.entity.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record StopRideRequest(
        @NotNull @Valid Location location
) {}
