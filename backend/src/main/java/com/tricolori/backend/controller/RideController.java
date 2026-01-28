package com.tricolori.backend.controller;

import com.tricolori.backend.dto.ride.OrderRequest;

import com.tricolori.backend.dto.ride.*;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.service.AuthService;
import com.tricolori.backend.service.InconsistencyReportService;
import com.tricolori.backend.service.ReviewService;
import com.tricolori.backend.service.RideService;
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

    // passenger leaves a rating for previous ride
    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> rateRide(
            @PathVariable Long id,
            @Valid @RequestBody RideRatingRequest request,
            @AuthenticationPrincipal Person passenger
    ) {
        reviewService.rateRide(id, passenger.getId(), request);
        return ResponseEntity.ok().build();
    }

    // checks whether the ride can be rated or already has been
    @GetMapping("/{id}/rating-status")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideRatingResponse> getRatingStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Person passenger
    ) {
        RideRatingResponse response = reviewService.getRatingStatus(id, passenger.getId());
        return ResponseEntity.ok(response);
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

    // detailed view for specific ride (for passenger)
    @GetMapping("/{id}/details/passenger")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PassengerRideDetailResponse> getPassengerRideDetail(@PathVariable Long id) {
        Long passengerId = authenticationService.getAuthenticatedUserId();
        PassengerRideDetailResponse detail = rideService.getPassengerRideDetail(id, passengerId);
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

    // TODO: REMOVE AFTER TESTING
    /*@PostMapping("/create/passenger")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<?> createRide(@RequestBody CreateRideRequest request) {
        // Hard-coded passenger ID for testing
        Long passengerId = 14L;

        Ride ride = rideService.createRide(request, passengerId);
        return ResponseEntity.ok(Map.of(
                "rideId", ride.getId(),
                "status", ride.getStatus(),
                "price", ride.getPrice(),
                "distance", ride.getRoute().getDistanceKm(),
                "estimatedTime", ride.getRoute().getEstimatedTimeSeconds()
        ));
    }*/

    @PostMapping("/order")
    // @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<String> order(
        @AuthenticationPrincipal Person passenger,
        @RequestBody OrderRequest request
    ) {
        rideService.rideOrder(request);
        return ResponseEntity.ok(request.toString());
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}