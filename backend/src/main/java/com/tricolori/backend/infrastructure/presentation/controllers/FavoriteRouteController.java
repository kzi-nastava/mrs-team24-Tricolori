package com.tricolori.backend.infrastructure.presentation.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.infrastructure.presentation.dtos.RouteDto;

@RestController
@RequestMapping("/api/v1/favorite-routes")
public class FavoriteRouteController {
    @GetMapping("/{userId}")
    public ResponseEntity<List<RouteDto>> getFavoriteRoutes(@PathVariable Long userId) {
        return ResponseEntity.ok().build();
    }
}
