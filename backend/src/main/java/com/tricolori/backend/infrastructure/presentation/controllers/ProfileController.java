package com.tricolori.backend.infrastructure.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.infrastructure.presentation.dtos.ProfileRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.ProfileResponse;


@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    // TODO: handle profile picture update... 

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getUserProfile(@AuthenticationPrincipal Person user) {
        ProfileResponse response = ProfileResponse.fromPerson(user);
        // TODO: If user is driver, load data about daily activity...
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileRequest request, @PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
