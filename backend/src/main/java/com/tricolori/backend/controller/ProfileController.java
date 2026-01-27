package com.tricolori.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.repository.DriverRepository;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.service.ChangeDataRequestService;
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
    private final ChangeDataRequestService requestService;
    private final CloudinaryService cloudinaryService;

    private final DriverRepository driverRepository;
    private final PersonRepository personRepository;

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
    @PreAuthorize("hasRole('ADMIN') or hasRole('PASSENGER')")
    public ResponseEntity<ProfileResponse> updateProfile(
        @AuthenticationPrincipal Person user,
        @RequestBody ProfileRequest request
    ) {
        Person dbPerson = personRepository.findById(user.getId())
            .orElseThrow(() -> new PersonNotFoundException("Person not found"));

        ProfileResponse updated = profileService.updateMyProfile(dbPerson, request);
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

    @PutMapping("/request-changes")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> requestChanges(
        @AuthenticationPrincipal Person user,
        @RequestBody ProfileRequest request
    ) {
        Person dbPerson = personRepository.findById(user.getId())
            .orElseThrow(() -> new PersonNotFoundException("Person not found"));

        Driver driver = driverRepository.findById(dbPerson.getId())
            .orElseThrow(() -> new PersonNotFoundException("Driver not found"));
        
        requestService.createRequest(driver, request);
        return ResponseEntity.ok().build();
    }
}
