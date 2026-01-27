package com.tricolori.backend.dto.profile;

public record PassengerDetailDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profilePicture,
        boolean isMainPassenger
) {}