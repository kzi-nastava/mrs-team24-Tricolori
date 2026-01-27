package com.tricolori.backend.dto.profile;

public record PassengerDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean isMainPassenger
) {}