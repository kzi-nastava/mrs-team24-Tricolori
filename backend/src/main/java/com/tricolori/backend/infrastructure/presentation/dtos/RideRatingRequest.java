package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRatingRequest {

    @NotNull(message = "Vehicle rating is required")
    @Min(value = 1, message = "Vehicle rating must be at least 1")
    @Max(value = 5, message = "Vehicle rating must be at most 5")
    private Integer vehicleRating;

    @NotNull(message = "Driver rating is required")
    @Min(value = 1, message = "Driver rating must be at least 1")
    @Max(value = 5, message = "Driver rating must be at most 5")
    private Integer driverRating;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;
}