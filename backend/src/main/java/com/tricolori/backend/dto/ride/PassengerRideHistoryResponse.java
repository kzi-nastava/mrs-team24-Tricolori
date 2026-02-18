package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;

public record PassengerRideHistoryResponse (
        Long id,
        String pickupAddress,
        String destinationAddress,
        LocalDateTime createdAt,
        String status,
        Double price
) {
}