package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewDto(
        @Min(1) @Max(5)
        Integer vehicleRating,

        @Min(1) @Max(5)
        Integer driverRating,

        @Size(max = 500)
        String comment
) {}