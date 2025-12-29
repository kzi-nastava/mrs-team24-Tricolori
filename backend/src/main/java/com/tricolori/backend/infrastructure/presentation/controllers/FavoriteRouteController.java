package com.tricolori.backend.infrastructure.presentation.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.core.domain.models.Address;
import com.tricolori.backend.infrastructure.presentation.dtos.RouteDto;

@RestController
@RequestMapping("/api/v1/favorite-routes")
public class FavoriteRouteController {
    @GetMapping("/{userId}")
    public ResponseEntity<List<RouteDto>> getFavoriteRoutes(@PathVariable Long userId) {
        // --- Route 1 ---
        Address pickup1 = new Address(
                "Nemanjina 12",
                "Beograd",
                20.4569,
                44.8076
        );

        Address destination1 = new Address(
                "Bulevar Oslobođenja 45",
                "Novi Sad",
                19.8335,
                45.2671
        );

        List<Address> stops1 = List.of(
                new Address("Autokomanda", "Beograd", 20.4750, 44.7880),
                new Address("Stara Pazova centar", "Stara Pazova", 20.1600, 44.9800)
        );

        RouteDto route1 = new RouteDto(pickup1, destination1, stops1);

        // --- Route 2 ---
        Address pickup2 = new Address(
                "Cara Dušana 10",
                "Novi Sad",
                19.8330,
                45.2550
        );

        Address destination2 = new Address(
                "Zmaj Jovina 3",
                "Novi Sad",
                19.8430,
                45.2558
        );

        List<Address> stops2 = List.of(
                new Address("Železnička stanica", "Novi Sad", 19.8270, 45.2640)
        );

        RouteDto route2 = new RouteDto(pickup2, destination2, stops2);

        return ResponseEntity.ok(List.of(route1, route2));
    }
}
