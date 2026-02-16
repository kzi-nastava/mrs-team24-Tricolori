package com.tricolori.backend.controller;

import com.tricolori.backend.dto.history.AdminRideHistoryResponse;
import com.tricolori.backend.dto.ride.*;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.service.AuthService;
import com.tricolori.backend.service.InconsistencyReportService;
import com.tricolori.backend.service.ReviewService;
import com.tricolori.backend.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PutMapping("/cancel")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<Void> cancelRide(
            @RequestBody CancelRideRequest request,
            @AuthenticationPrincipal Person person
    ) {

        rideService.cancelRide(person, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/panic")
    public ResponseEntity<Void> panicRide(
            @Valid @RequestBody PanicRideRequest request,
            @AuthenticationPrincipal Person person
    ) {

        rideService.panicRide(person, request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminRideHistoryResponse>> getAdminRideHistory(
            @RequestParam(required = false) String personEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<AdminRideHistoryResponse> history = rideService.getAdminRideHistory(personEmail, startDate, endDate, pageable);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/details/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RideDetailResponse> getAdminRideDetail(@PathVariable Long id) {
        RideDetailResponse detail = rideService.getAdminRideDetail(id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<RideDetailResponse> getRideDetails(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/stop")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<StopRideResponse> stopRide(
            @Valid @RequestBody StopRideRequest request,
            @AuthenticationPrincipal Person person
    ) {

        return ResponseEntity.ok(rideService.stopRide(person, request));
    }

    // passenger or driver can track current ride
    @GetMapping("/{id}/track")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    public ResponseEntity<RideTrackingResponse> trackRide(@PathVariable Long id) {
        RideTrackingResponse response = rideService.trackRide(id);
        return ResponseEntity.ok(response);
    }

    // driver sends data as the ride is finished
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

    @GetMapping("/passenger")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Page<PassengerRideHistoryResponse>> getPassengerHistory(
            @AuthenticationPrincipal Person person,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<PassengerRideHistoryResponse> history =
                rideService.getPassengerHistory(person, startDate, endDate, pageable);

        return ResponseEntity.ok(history);
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

    @PutMapping("/{rideId}/passenger-location")
    public ResponseEntity<Void> updatePassengerLocation(
            @PathVariable Long rideId,
            @Valid @RequestBody Location location
    ) {
        rideService.updatePassengerLocation(rideId, location.getLatitude(), location.getLongitude());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{rideId}/vehicle-location")
    public ResponseEntity<Void> updateVehicleLocation(
            @PathVariable Long rideId,
            @Valid @RequestBody Location location
    ) {
        rideService.updateVehicleLocation(rideId, location.getLatitude(), location.getLongitude());

        return ResponseEntity.ok().build();
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
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<String> order(
        @AuthenticationPrincipal Person passenger,
        @RequestBody OrderRequest request
    ) {
        try {
            rideService.rideOrder(passenger, request);
        } catch (Exception e) {
            String errorResponse = "ODGOVOR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            return ResponseEntity.ok(errorResponse);
        }
        return ResponseEntity.ok("Created a ride.");
    }

    @PutMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> startRide(
        @AuthenticationPrincipal Person driver,
        @PathVariable Long rideId
    ) {
        rideService.startRide(driver, rideId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ongoing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RideTrackingResponse>> getAllOngoingRides() {
        List<RideTrackingResponse> ongoingRides = rideService.getAllOngoingRides();
        return ResponseEntity.ok(ongoingRides);
    }
}