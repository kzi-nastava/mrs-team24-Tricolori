package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Address;

public record RideEstimationResponse(

        Address pickupLocation,
        Address destinationLocation,

        Long estimatedTimeSeconds, // e.g. 900 (15 min)
        Double estimatedDistanceKm, // e.g. 5.2
        Double estimatedPrice,      // e.g. 650.00

        String routeGeometry
) {}