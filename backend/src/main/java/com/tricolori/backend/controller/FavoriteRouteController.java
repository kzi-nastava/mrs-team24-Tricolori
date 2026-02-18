package com.tricolori.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tricolori.backend.service.FavoriteRouteService;

import lombok.RequiredArgsConstructor;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.dto.route.FavoriteRouteRequest;
import com.tricolori.backend.dto.route.FavoriteRouteResponse;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/favorite-routes")
@RequiredArgsConstructor
public class FavoriteRouteController {
    private final FavoriteRouteService service;

    @GetMapping()
    public ResponseEntity<List<FavoriteRouteResponse>> getFavoriteRoutes(
        @AuthenticationPrincipal Person person
    ) {
        List<FavoriteRouteResponse> response = service.getFavoriteRoutes(person);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> saveFavoriteRoute(
        @AuthenticationPrincipal Person person,
        @RequestBody FavoriteRouteRequest request
    ) {
        service.addFavoriteRoute(person, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{routeId}")
    public ResponseEntity<Void> removeFavoriteRoute(
        @AuthenticationPrincipal Person person,
        @PathVariable Long routeId
    ) {
        service.removeFavoriteRoute(person, routeId);
        return ResponseEntity.noContent().build();
    }
}
