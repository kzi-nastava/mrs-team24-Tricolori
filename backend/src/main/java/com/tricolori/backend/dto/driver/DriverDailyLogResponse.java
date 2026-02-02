package com.tricolori.backend.dto.driver;

import java.time.LocalDate;

public record DriverDailyLogResponse(
        Long id,
        Long driverId,
        LocalDate date,
        Long activeTimeSeconds,
        boolean active
) {
}
