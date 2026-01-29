package com.tricolori.backend.controller;

import com.tricolori.backend.dto.pricelist.PriceConfigRequest;
import com.tricolori.backend.dto.pricelist.PriceConfigResponse;
import com.tricolori.backend.service.PriceListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pricelist")
@RequiredArgsConstructor
public class PricelistController {

    private final PriceListService priceListService;

    // Get current pricing configuration - Returns all base prices and the km price
    @GetMapping
    public ResponseEntity<PriceConfigResponse> getCurrentPricing() {
        PriceConfigResponse response = priceListService.getCurrentPricing();
        return ResponseEntity.ok(response);
    }

    // Update pricing (admin only) - Admin can define/change prices for all vehicle types and km price
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePricing(@Valid @RequestBody PriceConfigRequest request) {
        priceListService.updatePricing(request);
        return ResponseEntity.ok().build();
    }
}