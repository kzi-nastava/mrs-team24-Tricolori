package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;


public record OrderRequest(
    RideRoute route,
    RidePreferences preferences,
    RideEstimations estimations,
    LocalDateTime createdAt,
    String[] trackers
) 
{}
