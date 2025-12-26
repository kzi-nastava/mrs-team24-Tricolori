package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Address;
import com.tricolori.backend.shared.enums.RideStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RideHistoryResponse(
        Long rideId,

        LocalDateTime startTime,
        LocalDateTime endTime,

        Address pickupAddress,
        Address destinationAddress,

        Double price,

        RideStatus rideStatus
) {}
