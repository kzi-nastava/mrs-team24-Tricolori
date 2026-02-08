package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;

public record RideHistoryFilter(
        Long personId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
) {}