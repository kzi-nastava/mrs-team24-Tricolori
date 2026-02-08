package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;

public record RideRatingResponse(
        boolean canRate,
        boolean alreadyRated,
        boolean deadlinePassed,
        LocalDateTime deadline
) {}
