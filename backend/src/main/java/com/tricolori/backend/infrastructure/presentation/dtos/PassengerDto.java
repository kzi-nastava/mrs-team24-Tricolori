package com.tricolori.backend.infrastructure.presentation.dtos;

public record PassengerDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean isMainPassenger
) {}