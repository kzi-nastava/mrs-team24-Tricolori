package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.core.domain.models.Address;
import com.tricolori.backend.infrastructure.presentation.dtos.*;
import com.tricolori.backend.shared.enums.RideStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    @PostMapping("/estimate")
    public ResponseEntity<RideEstimationResponse> estimateRide(@Valid @RequestBody RideEstimationRequest request) {

        Address pickup = new Address(
                "Bulevar oslobodjenja, 12", "Novi Sad",
                45.2551, 19.8452
        );

        Address destination = new Address(
                "Narodnog fronta, 23", "Novi Sad",
                45.2403, 19.8227
        );

        String routeGeometry = "u{~fG~_~|Ap@_@z@y@|@o@`@Y^O`@M";

        RideEstimationResponse response = new RideEstimationResponse(
                pickup,
                destination,
                600L,
                3.5,
                450.0,
                routeGeometry
        );

        return ResponseEntity.ok(response);
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

        Address pickup1 = new Address("Danila Kisa, 10", "Novi Sad", 19.83, 45.25);
        Address dest1 = new Address("Futoska, 55", "Novi Sad", 19.82, 45.23);

        Address pickup2 = new Address("Zeleznicka, 4", "Novi Sad", 19.84, 45.25);
        Address dest2 = new Address("Bulevar Evrope, 15", "Novi Sad", 19.80, 45.24);

        List<RideHistoryResponse> rides = List.of(
                new RideHistoryResponse(
                        101L,
                        LocalDateTime.now().minusDays(1).minusMinutes(30),
                        LocalDateTime.now().minusDays(1),
                        pickup1,
                        dest1,
                        540.0,
                        RideStatus.FINISHED
                ),
                new RideHistoryResponse(
                        102L,
                        LocalDateTime.now().minusHours(2),
                        LocalDateTime.now().minusHours(2).plusMinutes(5),
                        pickup2,
                        dest2,
                        0.0,
                        RideStatus.CANCELLED_BY_DRIVER
                ),
                new RideHistoryResponse(
                        103L,
                        LocalDateTime.now().minusHours(5),
                        LocalDateTime.now().minusHours(4).plusMinutes(45),
                        dest1,
                        pickup1,
                        1200.0,
                        RideStatus.FINISHED
                )
        );

        Page<RideHistoryResponse> page = new PageImpl<>(rides, pageable, rides.size());

        return ResponseEntity.ok(page);
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


    @PostMapping("/order")
    public ResponseEntity<Void> order(@RequestBody OrderRequest request) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
