package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InconsistencyReportRequest(
        @NotBlank
        @Size(min = 10, max = 500)
        String description
) {}
