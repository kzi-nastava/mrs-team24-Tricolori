package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.core.domain.models.Address;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

        Address pickup = new Address(
                "Bulevar Oslobođenja 12",
                "Novi Sad",
                19.8335,
                45.2671
        );

        Address destination = new Address(
                "Zmaj Jovina 4",
                "Novi Sad",
                19.8402,
                45.2558
        );

        Address stop1 = new Address(
                "Futoška 45",
                "Novi Sad",
                19.8260,
                45.2620
        );

        RouteDto route = new RouteDto(
                pickup,
                destination,
                List.of(stop1)   // order matters
        );

        RideTrackingResponse response = new RideTrackingResponse(
                id,
                "IN_PROGRESS",

                new VehicleLocationResponse(
                        1L,
                        "Toyota Corolla",
                        "NS-123-AB",
                        45.2665,
                        19.8340,
                        false,
                        new VehicleSpecificationDto(
                                "Sedan",
                                5,
                                true,
                                false
                        )
                ),

                route,

                8,
                LocalDateTime.now().plusMinutes(8),
                null,
                LocalDateTime.now().minusMinutes(5),
                650.0,

                new DriverDto(
                        10L,
                        "Marko",
                        "Petrović",
                        "https://google.drive.com/epfjeoirfaspd243.jpg",
                        3.7
                ),

                List.of(
                        new PassengerDto(20L, "Ana", "Jovanović", "anajo@example.com", true)
                )
        );

        return ResponseEntity.ok(response);
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

        RideReviewResponse response = new RideReviewResponse(
                true,                       // canRate
                false,                      // alreadyRated
                false,                      // deadlinePassed
                LocalDateTime.now().plusHours(24)
        );

        return ResponseEntity.ok(response);
    }


    // get driver's ride history
    @GetMapping("/history/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Object>> getDriverHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        List<Object> dummyList = List.of(
                Map.of(
                        "rideId", 1L,
                        "passengerName", "John Doe",
                        "createdAt", LocalDateTime.now().minusDays(1),
                        "startLocation", "Main Street 1",
                        "endLocation", "Park Avenue 5",
                        "price", 12.50
                ),
                Map.of(
                        "rideId", 2L,
                        "passengerName", "Jane Smith",
                        "createdAt", LocalDateTime.now().minusDays(2),
                        "startLocation", "Central Square 3",
                        "endLocation", "River Road 7",
                        "price", 18.75
                )
        );

        return ResponseEntity.ok(dummyList);
    }


    // detailed view for specific ride
    @GetMapping("/{id}/details/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Object> getDriverRideDetail(@PathVariable Long id) {
        Map<String, Object> dummyDetail = Map.of(
                "rideId", id,
                "driverName", "Alice Driver",
                "passengerName", "John Doe",
                "createdAt", LocalDateTime.now().minusDays(1),
                "startLocation", "Main Street 1",
                "endLocation", "Park Avenue 5",
                "price", 12.50,
                "driverRating", 5,
                "vehicleRating", 4,
                "comment", "Smooth ride!"
        );

        return ResponseEntity.ok(dummyDetail);
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
    public ResponseEntity<Object> getRideStatus(@PathVariable Long id) {
        Map<String, Object> dummyStatus = Map.of(
                "rideId", id,
                "driverName", "Alice Driver",
                "passengerName", "John Doe",
                "status", "IN_PROGRESS",   // example statuses: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
                "startLocation", "Main Street 1",
                "endLocation", "Park Avenue 5",
                "startedAt", LocalDateTime.now().minusMinutes(15),
                "estimatedEndAt", LocalDateTime.now().plusMinutes(20)
        );

        return ResponseEntity.ok(dummyStatus);
    }

    // find current driver's ride (for admin)
    @GetMapping("/current/driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getCurrentRideByDriver(@PathVariable Long driverId) {
        Map<String, Object> dummyCurrentRide = Map.of(
                "rideId", 12345L,
                "driverId", driverId,
                "driverName", "Alice Driver",
                "passengerName", "Jane Smith",
                "status", "IN_PROGRESS",
                "startLocation", "Central Square 3",
                "endLocation", "River Road 7",
                "startedAt", LocalDateTime.now().minusMinutes(10),
                "estimatedEndAt", LocalDateTime.now().plusMinutes(25)
        );

        return ResponseEntity.ok(dummyCurrentRide);
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
