package com.tricolori.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.dto.block.BlockRequest;
import com.tricolori.backend.service.PersonService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/blocks")
public class PersonController {
    private final PersonService service;

    @PatchMapping("/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockUser(
        @Valid @RequestBody BlockRequest request
    ) {
        service.applyBlock(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockUser(
        @RequestParam @Email String email
    ) {
        service.removeBlock(email);
        return ResponseEntity.noContent().build();
    }
}
