package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import java.time.LocalDateTime;

public record RideHistoryFilter(
        Long personId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
) {}