package com.tricolori.backend.dto.ride;

import jakarta.validation.constraints.NotNull;

public record StopRideResponse(
   @NotNull Double updatedPrice
) {}
