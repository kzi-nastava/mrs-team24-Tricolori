package com.tricolori.backend.event;

import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.RideStatus;
import lombok.Data;

import java.util.List;

@Data
public class RideStatusUpdateEvent {
    private final List<String> passengerEmails;
    private final Long rideId;
    private final RideStatus status;
    private final String message;

    public RideStatusUpdateEvent(Ride ride, RideStatus status, String message) {
        this.passengerEmails = ride.getPassengers().stream()
                .map(Passenger::getEmail)
                .toList();
        this.rideId = ride.getId();
        this.status = status;
        this.message = message;
    }
}