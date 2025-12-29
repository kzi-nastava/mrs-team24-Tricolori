package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.VehicleLocationResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.VehicleSpecificationDto;
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

        List<VehicleLocationResponse> vehicles = List.of(
                new VehicleLocationResponse(
                        1L,
                        "Toyota Corolla",
                        "NS-123-AB",
                        45.2671,
                        19.8335,
                        true,
                        new VehicleSpecificationDto(
                                "Sedan",
                                5,
                                true,
                                false
                        )
                ),
                new VehicleLocationResponse(
                        2L,
                        "Volkswagen Golf",
                        "NS-456-CD",
                        45.2550,
                        19.8450,
                        false,
                        new VehicleSpecificationDto(
                                "Hatchback",
                                5,
                                false,
                                true
                        )
                ),
                new VehicleLocationResponse(
                        3L,
                        "Skoda Octavia",
                        "NS-789-EF",
                        45.2620,
                        19.8200,
                        true,
                        new VehicleSpecificationDto(
                                "Wagon",
                                5,
                                true,
                                true
                        )
                )
        );

        return ResponseEntity.ok(vehicles);
    }

    // get real time location of a vehicle for tracking
    @GetMapping("/{id}/location")
    public ResponseEntity<VehicleLocationResponse> getVehicleLocation(@PathVariable Long id) {

        VehicleLocationResponse vehicle = new VehicleLocationResponse(
                id,
                "Toyota Corolla",
                "NS-123-AB",
                45.2671,
                19.8335,
                true,
                new VehicleSpecificationDto(
                        "Sedan",
                        5,
                        true,
                        false
                )
        );

        return ResponseEntity.ok(vehicle);
    }
}
