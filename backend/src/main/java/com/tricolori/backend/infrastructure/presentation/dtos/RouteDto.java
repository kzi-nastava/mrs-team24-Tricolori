package com.tricolori.backend.infrastructure.presentation.dtos;

import java.util.List;

public record RouteDto(
    LocationDto pickup,
    LocationDto destination,
    List<LocationDto> stops     // order in list determines the order in stops
) 
{}
