package com.tricolori.backend.infrastructure.presentation.dtos;

import java.time.LocalDate;

public record PriceConfigResponse(
        Double standardPrice,
        Double luxuryPrice,
        Double vanPrice,
        Double kmPrice,
        LocalDate createdAt
) {}