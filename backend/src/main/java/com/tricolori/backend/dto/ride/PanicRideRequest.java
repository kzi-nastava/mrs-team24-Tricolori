package com.tricolori.backend.dto.ride;

import com.tricolori.backend.entity.Location;

public record PanicRideRequest(
        Location vehicleLocation
) {
}
