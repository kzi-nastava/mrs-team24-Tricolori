package com.tricolori.backend.infrastructure.presentation.dtos.Route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSRMResult {
    double distanceKilometers;
    long durationSeconds;
    String geometry;
}
