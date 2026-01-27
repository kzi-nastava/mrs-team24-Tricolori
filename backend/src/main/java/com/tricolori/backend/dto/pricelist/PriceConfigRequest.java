package com.tricolori.backend.dto.pricelist;

import jakarta.validation.constraints.Min;

public record PriceConfigRequest(
        @Min(0)
        Double standardPrice,

        @Min(0)
        Double luxuryPrice,

        @Min(0)
        Double vanPrice,

        @Min(0)
        Double kmPrice
) {}