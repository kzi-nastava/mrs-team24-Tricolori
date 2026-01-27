package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import java.util.List;

import com.tricolori.backend.core.domain.models.Stop;

public record RideRoute(
    Stop pickup,
    Stop destination,
    List<Stop> stops
) 
{}
