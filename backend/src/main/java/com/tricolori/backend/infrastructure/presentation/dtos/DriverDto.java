package com.tricolori.backend.infrastructure.presentation.dtos;

public record DriverDto(
        Long id,
        String firstName,
        String lastName,
        String profilePicture,
        Double rating
) {}