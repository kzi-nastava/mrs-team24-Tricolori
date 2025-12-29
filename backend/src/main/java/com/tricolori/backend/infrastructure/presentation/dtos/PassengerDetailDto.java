package com.tricolori.backend.infrastructure.presentation.dtos;

public record PassengerDetailDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profilePicture,
        boolean isMainPassenger
) {}