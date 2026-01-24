package com.tricolori.backend.infrastructure.presentation.dtos.Auth;

import jakarta.validation.constraints.NotBlank;

public record DriverPasswordSetupRequest (
    @NotBlank String token,
    @NotBlank String password
)
{}
