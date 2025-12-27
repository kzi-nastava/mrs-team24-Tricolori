package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.NotNull;

public record StopRideResponse(
   @NotNull Double updatedPrice
) {}
