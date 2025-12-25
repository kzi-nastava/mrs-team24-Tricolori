package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.RideEstimationRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.RideEstimationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    @PostMapping("/estimate")
    public ResponseEntity<RideEstimationResponse> estimateRide(@Valid @RequestBody RideEstimationRequest request) {

        return ResponseEntity.ok().build();
    }
}
