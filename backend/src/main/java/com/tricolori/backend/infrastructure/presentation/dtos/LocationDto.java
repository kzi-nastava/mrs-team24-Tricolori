package com.tricolori.backend.infrastructure.presentation.dtos;

public record LocationDto(
   String address,
   Double latitude,
   Double longitude
) {}
