package com.tricolori.backend.infrastructure.presentation.dtos;

public record ReviewDto(
        int rating,       // 1-5
        String comment,
        String type       // driver or vehicle review
) {}