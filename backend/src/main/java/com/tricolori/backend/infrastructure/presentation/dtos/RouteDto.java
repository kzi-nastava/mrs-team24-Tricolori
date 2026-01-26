package com.tricolori.backend.infrastructure.presentation.dtos;

import java.util.List;

import com.tricolori.backend.core.domain.models.Stop;

public record RouteDto(
        Stop pickupStop,
        Stop destinationStop,
        List<Stop> stops     // order in list determines the order in stops
) 
{}
