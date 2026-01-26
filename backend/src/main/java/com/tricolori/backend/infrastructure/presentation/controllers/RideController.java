package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.services.AuthService;
import com.tricolori.backend.core.services.InconsistencyReportService;
import com.tricolori.backend.core.services.ReviewService;
import com.tricolori.backend.core.services.RideService;
import com.tricolori.backend.infrastructure.presentation.dtos.*;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;
    private final ReviewService reviewService;
    private final InconsistencyReportService inconsistencyReportService;
    private final AuthService authenticationService;

    @PostMapping("/estimate")
    public ResponseEntity<RideEstimationResponse> estimateRide(@Valid @RequestBody RideEstimationRequest request) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRide(
            @PathVariable Long id,
            @RequestBody CancelRideRequest request,
            Authentication authentication
    ) {

        String personEmail =  authentication.getName();
        rideService.cancelRide(id, personEmail, request);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/panic")
    public ResponseEntity<Void> panicRide(
            @PathVariable Long id,
            @Valid @RequestBody PanicRideRequest request,
            Authentication authentication
    ) {

        String personEmail = authentication.getName();
        rideService.panicRide(id, personEmail, request);

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
    public ResponseEntity<StopRideResponse> stopRide(
            @PathVariable Long id,
            @Valid @RequestBody StopRideRequest request,
            @AuthenticationPrincipal Person person
    ) {

        return ResponseEntity.ok(rideService.stopRide(id, person, request));
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<RideTrackingResponse> trackRide(@PathVariable Long id) {
        RideTrackingResponse response = rideService.trackRide(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> completeRide(@PathVariable Long id) {
        Long driverId = authenticationService.getAuthenticatedUserId();
        rideService.completeRide(id, driverId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> rateRide(
            @PathVariable Long id,
            @Valid @RequestBody RideRatingRequest request
    ) {
        Long passengerId = authenticationService.getAuthenticatedUserId();
        reviewService.rateRide(id, passengerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/rating-status")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideRatingResponse> getRatingStatus(@PathVariable Long id) {
        Long passengerId = authenticationService.getAuthenticatedUserId();
        reviewService.getRatingStatus(id, passengerId);
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
        Long driverId = authenticationService.getAuthenticatedUserId();

        Pageable pageable = Pageable.unpaged();

        List<RideHistoryResponse> history =
                rideService.getDriverHistory(driverId, pageable)
                        .getContent();

        return ResponseEntity.ok(history);
    }

    // detailed view for specific ride
    @GetMapping("/{id}/details/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDetailResponse> getDriverRideDetail(@PathVariable Long id) {
        Long driverId = authenticationService.getAuthenticatedUserId();
        RideDetailResponse detail = rideService.getDriverRideDetail(id, driverId);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/report-inconsistency")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> reportInconsistency(
            @PathVariable Long id,
            @Valid @RequestBody InconsistencyReportRequest request
    ) {
        Long passengerId = authenticationService.getAuthenticatedUserId();

        inconsistencyReportService.reportInconsistency(
                id,
                passengerId,
                request
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RideStatusResponse> getRideStatus(@PathVariable Long id) {
        RideStatusResponse response = rideService.getRideStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current/driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RideStatusResponse> getCurrentRideByDriver(
            @PathVariable Long driverId
    ) {
        RideStatusResponse response =
                rideService.getCurrentRideByDriver(driverId);

        return ResponseEntity.ok(response);
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