package com.tricolori.backend.infrastructure.presentation.dtos;

import java.time.LocalDateTime;

public record RideReviewResponse(
        boolean canRate,
        boolean alreadyRated,
        boolean deadlinePassed,
        LocalDateTime deadline
) {}
