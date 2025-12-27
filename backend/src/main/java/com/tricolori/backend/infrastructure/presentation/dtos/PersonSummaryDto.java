package com.tricolori.backend.infrastructure.presentation.dtos;

public record PersonSummaryDto(
        Long id,
        String email,
        String fullName,
        String profilePictureUrl
) {}