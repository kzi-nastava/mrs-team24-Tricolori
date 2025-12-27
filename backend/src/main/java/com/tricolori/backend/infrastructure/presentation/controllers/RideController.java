package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/history")
    public ResponseEntity<Page<RideHistoryResponse>> getAdminRideHistory(
            @RequestBody RideHistoryFilter filter,
            Pageable pageable
    ) {

        return ResponseEntity.ok(Page.empty());
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<RideDetailResponse> getRideDetails(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }
      
    @PutMapping("/{id}/stop")
    public ResponseEntity<StopRideResponse> stopRide(@Valid @RequestBody StopRideRequest request, @PathVariable Long id) {

        return ResponseEntity.ok().build();
    }


    @PostMapping("/order")
    public ResponseEntity<Void> order(@RequestBody OrderRequest request) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
