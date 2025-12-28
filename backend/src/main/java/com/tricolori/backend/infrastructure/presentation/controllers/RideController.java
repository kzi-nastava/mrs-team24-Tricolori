package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    // track ride in real time for passengers
    @GetMapping("/{id}/track")
    public ResponseEntity<RideTrackingResponse> trackRide(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }

    // finish the ride (driver)
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> completeRide(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }

    // leave a rating for driver and vehicle
    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> rateRide(
            @PathVariable Long id,
            @Valid @RequestBody RideRatingRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    // get ride rating status - because of the deadline
    @GetMapping("/{id}/rating-status")
    public ResponseEntity<RideReviewResponse> getRatingStatus(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }

    // get driver's ride history
    @GetMapping("/history/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideHistoryResponse>> getDriverHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        return ResponseEntity.ok(List.of());
    }

    // detailed view for specific ride
    @GetMapping("/{id}/details/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDetailResponse> getDriverRideDetail(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }

    // report driver inconsistency (passenger during ride)
    @PostMapping("/{id}/report-inconsistency")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> reportInconsistency(
            @PathVariable Long id,
            @Valid @RequestBody InconsistencyReportRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    // current ride status so admin can monitor
    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RideStatusResponse> getRideStatus(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }

    // find current driver's ride (for admin)
    @GetMapping("/current/driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RideStatusResponse> getCurrentRideByDriver(@PathVariable Long driverId) {

        return ResponseEntity.ok().build();
    }

}
