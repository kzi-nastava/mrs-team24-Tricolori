package com.tricolori.backend.controller;

import com.tricolori.backend.dto.ride.RideTrackingResponse;
import com.tricolori.backend.entity.TrackingToken;
import com.tricolori.backend.service.RideService;
import com.tricolori.backend.service.TrackingTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class RideTrackingController {

    private final TrackingTokenService trackingTokenService;
    private final RideService rideService;

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            TrackingToken trackingToken = trackingTokenService.validateToken(token);
            boolean isRegistered = trackingTokenService.isRegisteredUser(trackingToken.getEmail());

            return ResponseEntity.ok(new TokenValidationResponse(
                    true,
                    trackingToken.getRide().getId(),
                    isRegistered,
                    trackingToken.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new TokenValidationResponse(false, null, false, null));
        }
    }

    @GetMapping("/ride/{token}")
    public ResponseEntity<RideTrackingResponse> trackRideByToken(@PathVariable String token) {
        TrackingToken trackingToken = trackingTokenService.validateToken(token);
        RideTrackingResponse response = rideService.trackRide(trackingToken.getRide().getId());
        return ResponseEntity.ok(response);
    }

    record TokenValidationResponse(boolean valid, Long rideId, boolean isRegistered, String email) {}
}