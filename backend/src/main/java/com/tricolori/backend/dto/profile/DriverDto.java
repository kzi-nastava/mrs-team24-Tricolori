package com.tricolori.backend.dto.profile;

public record DriverDto(
        Long id,
        String firstName,
        String lastName,
        String profilePicture,
        Double rating
) {}