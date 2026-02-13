package com.tricolori.backend.dto.history;

import java.time.LocalDateTime;

public record AdminRideHistoryResponse (
        Long id,
        String pickupAddress,
        String destinationAddress,
        LocalDateTime createdAt,
        String status,
        Double price
) {
}