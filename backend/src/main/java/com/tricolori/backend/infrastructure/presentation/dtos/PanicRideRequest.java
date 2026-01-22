package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Address;

public record PanicRideRequest(
        Address vehicleLocation
) {
}
