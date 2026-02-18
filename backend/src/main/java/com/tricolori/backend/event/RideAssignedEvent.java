package com.tricolori.backend.event;

import com.tricolori.backend.entity.Ride;
import lombok.Data;

@Data
public class RideAssignedEvent {
    private final Long rideId;
    private final String driverEmail;

    public RideAssignedEvent(Ride ride) {
        this.rideId = ride.getId();
        this.driverEmail = ride.getDriver().getEmail();
    }
}