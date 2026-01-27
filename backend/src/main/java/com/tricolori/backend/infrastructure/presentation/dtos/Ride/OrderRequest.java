package com.tricolori.backend.infrastructure.presentation.dtos.Ride;

import java.time.LocalDateTime;


public record OrderRequest(
    RideRoute route,
    RidePreferences preferences,
    RideEstimations estimations,
    LocalDateTime createdAt,
    String[] trackers
) 
{}
