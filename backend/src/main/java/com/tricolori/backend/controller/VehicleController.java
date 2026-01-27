package com.tricolori.backend.controller;

import com.tricolori.backend.service.VehicleService;
import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // get all active vehicles to display on home page
    @GetMapping("/active")
    public ResponseEntity<List<VehicleLocationResponse>> getAllActiveVehicles() {
        List<VehicleLocationResponse> vehicles = vehicleService.getAllActiveVehicles();
        return ResponseEntity.ok(vehicles);
    }

    // get real time location of a vehicle for tracking
    @GetMapping("/{id}/location")
    public ResponseEntity<VehicleLocationResponse> getVehicleLocation(@PathVariable Long id) {

        return ResponseEntity.ok().build();
    }
}