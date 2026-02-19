package com.tricolori.backend.dto.ride;

import com.tricolori.backend.enums.RideStatus;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class RideUpdateMessage {
    private RideStatus status;
    private Long rideId;
    private String message;
    private LocalDateTime timestamp;

    public RideUpdateMessage(RideStatus status, Long rideId, String message) {
        this.status = status;
        this.rideId = rideId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}