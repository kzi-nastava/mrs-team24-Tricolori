package com.tricolori.backend.dto.ride;

import java.util.List;

import com.tricolori.backend.entity.Stop;

public record RideRoute(
    Stop pickup,
    Stop destination,
    List<Stop> stops
) 
{}
