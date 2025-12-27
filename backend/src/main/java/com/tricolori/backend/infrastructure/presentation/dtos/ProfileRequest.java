package com.tricolori.backend.infrastructure.presentation.dtos;

public record ProfileRequest(
    String firstName,
    String lastName,
    String homeAddress,
    String phoneNumber
) 
{}
