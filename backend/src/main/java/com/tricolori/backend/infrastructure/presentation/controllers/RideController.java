package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.CancelRideRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.RideEstimationRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.RideEstimationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    @PostMapping("/estimate")
    public ResponseEntity<RideEstimationResponse> estimateRide(@Valid @RequestBody RideEstimationRequest request) {

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRide(@Valid @RequestBody CancelRideRequest request, @PathVariable Long id) {

        return ResponseEntity.ok().build();
    }
}
