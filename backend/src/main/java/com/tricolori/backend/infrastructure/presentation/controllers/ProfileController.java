package com.tricolori.backend.infrastructure.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.infrastructure.presentation.dtos.ProfileRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.ProfileResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.VehicleDto;


@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    // TODO: handle profile picture update... 

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable Long id) {
        VehicleDto vehicle = new VehicleDto(
            "Toyota Corolla",
            "SEDAN",
            "BG-123-AB",
            4,
            true,
            false
        );

        ProfileResponse response = new ProfileResponse(
                "ana.jovanovic@example.com",
                "Jana",
                "Jovanovic",
                "Bulevar Oslobođenja 45, Novi Sad",
                "+381651112233",
                "https://cdn.example.com/profile/ana.png",
                vehicle,
                6.5
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileRequest request, @PathVariable Long id) {
        VehicleDto vehicle = new VehicleDto(
            "Toyota Corolla",
            "SEDAN",
            "BG-123-AB",
            4,
            true,
            false
        );

        ProfileResponse response = new ProfileResponse(
                "ana.jovanovic@example.com",
                request.firstName(),
                request.lastName(),
                request.homeAddress(),
                request.phoneNumber(),
                "https://cdn.example.com/profile/ana.png",
                vehicle,
                6.5
        );

        return ResponseEntity.ok(response);
    }
}
