package com.tricolori.backend.dto.ride;

import java.util.List;

import com.tricolori.backend.entity.Stop;

public record RouteDto(
    Stop pickupStop,
    Stop destinationStop,
    List<Stop> stops     // order in list determines the order in stops
) 
{}
