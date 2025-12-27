package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Address;
import com.tricolori.backend.shared.enums.RideStatus;
import java.time.LocalDateTime;
import java.util.List;

public record RideDetailResponse(
        String routeGeometry,

        Address pickupAddress,
        Address destinationAddress,

        LocalDateTime startTime,
        LocalDateTime endTime,

        Double totalCost,

        PersonSummaryDto driver,
        List<PersonSummaryDto> passengers,

        RideStatus status,
        List<String> inconsistencies,
        List<ReviewDto> reviews
) {}