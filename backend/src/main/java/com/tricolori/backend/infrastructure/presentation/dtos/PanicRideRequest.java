package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Location;

public record PanicRideRequest(
        Location vehicleLocation
) {
}
