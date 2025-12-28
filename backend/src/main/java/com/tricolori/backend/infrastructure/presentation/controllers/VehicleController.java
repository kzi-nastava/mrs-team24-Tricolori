package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.VehicleLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    // get all active vehicles to display on home page
    @GetMapping("/active")
    public ResponseEntity<List<VehicleLocationResponse>> getAllActiveVehicles() {

        return ResponseEntity.ok(List.of());
    }

    // get real time location of a vehicle for tracking
    @GetMapping("/{id}/location")
    public ResponseEntity<VehicleLocationResponse> getVehicleLocation(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }
}