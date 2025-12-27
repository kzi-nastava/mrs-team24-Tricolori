package com.tricolori.backend.infrastructure.presentation.dtos;

import java.time.LocalDateTime;

public record InconsistencyReportDto(
        Long id,
        String description,
        LocalDateTime reportedAt,
        String reportedBy
) {}