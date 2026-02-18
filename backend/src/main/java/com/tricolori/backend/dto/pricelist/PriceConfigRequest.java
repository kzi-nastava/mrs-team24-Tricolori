package com.tricolori.backend.dto.pricelist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PriceConfigRequest(
        @NotNull @Min(0)
        Double standardPrice,

        @NotNull @Min(0)
        Double luxuryPrice,

        @NotNull @Min(0)
        Double vanPrice,

        @NotNull @Min(0)
        Double kmPrice
) {}