package com.tricolori.backend.infrastructure.presentation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.services.ProfileService;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileResponse;


@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    @Autowired
    private ProfileService profileService;
    // TODO: handle profile picture update... 

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getUserProfile(@AuthenticationPrincipal Person user) {
        ProfileResponse response = ProfileResponse.fromPerson(user);
        // TODO: If user is driver, load data about daily activity...
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
        @AuthenticationPrincipal Person user,
        @RequestBody ProfileRequest request
    ) {
        ProfileResponse updated = profileService.updateMyProfile(user, request);
        return ResponseEntity.ok(updated);
    }
}
