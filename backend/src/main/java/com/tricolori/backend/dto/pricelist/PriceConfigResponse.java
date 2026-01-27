package com.tricolori.backend.dto.pricelist;

import java.time.LocalDate;

public record PriceConfigResponse(
        Double standardPrice,
        Double luxuryPrice,
        Double vanPrice,
        Double kmPrice,
        LocalDate createdAt
) {}