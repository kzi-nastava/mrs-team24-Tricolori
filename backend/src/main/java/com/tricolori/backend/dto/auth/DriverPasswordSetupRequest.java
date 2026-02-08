package com.tricolori.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record DriverPasswordSetupRequest (
    @NotBlank String token,
    @NotBlank String password
)
{}
