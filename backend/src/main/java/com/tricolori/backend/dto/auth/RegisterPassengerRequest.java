package com.tricolori.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterPassengerRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String homeAddress,
    @NotBlank String phoneNum
) {}
