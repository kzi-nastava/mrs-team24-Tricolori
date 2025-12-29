package com.tricolori.backend.infrastructure.presentation.dtos;

import java.time.LocalDateTime;

public record RideHistoryFilter(
        Long personId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
) {}