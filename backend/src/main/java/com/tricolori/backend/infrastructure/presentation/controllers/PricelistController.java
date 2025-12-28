package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.PriceConfigRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.PriceConfigResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pricelist")
@RequiredArgsConstructor
public class PricelistController {

    /**
     * 2.14 - Get current pricing configuration
     * Returns all base prices and the km price
     */
    @GetMapping
    public ResponseEntity<PriceConfigResponse> getCurrentPricing() {

        return ResponseEntity.ok().build();
    }

    /**
     * 2.14 - Update pricing (admin only)
     * Admin can define/change prices for all vehicle types and km price
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePricing(@Valid @RequestBody PriceConfigRequest request) {

        return ResponseEntity.ok().build();
    }
}