package com.tricolori.backend.dto.profile;

public record PersonSummaryDto(
        Long id,
        String email,
        String fullName,
        String profilePictureUrl
) {}