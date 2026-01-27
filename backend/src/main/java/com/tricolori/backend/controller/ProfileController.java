package com.tricolori.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.service.CloudinaryService;
import com.tricolori.backend.service.ProfileService;
import com.tricolori.backend.service.VehicleService;
import com.tricolori.backend.dto.profile.ProfileRequest;
import com.tricolori.backend.dto.profile.ProfileResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final VehicleService vehicleService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getUserProfile(@AuthenticationPrincipal Person user) {
        ProfileResponse response = ProfileResponse.fromPerson(user);
        
        boolean isDriver = user.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"));

        if(isDriver) {
            vehicleService.fillDriverVehicleData(user, response);
        }

        // TODO: add activity hours...
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

    @PostMapping("/upload-pfp")
    public ResponseEntity<Map<String, String>> uploadPfp(
        @AuthenticationPrincipal Person user,
        @RequestParam("pfp") MultipartFile pfpFile
    ) {
        String url = cloudinaryService.uploadProfilePicture(pfpFile, user.getId());
        return ResponseEntity.ok(Map.of("url", url));
    }
}
