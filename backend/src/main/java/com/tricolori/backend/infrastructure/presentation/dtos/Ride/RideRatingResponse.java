package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import java.time.LocalDateTime;

public record RideRatingResponse(
        boolean canRate,
        boolean alreadyRated,
        boolean deadlinePassed,
        LocalDateTime deadline
) {}
