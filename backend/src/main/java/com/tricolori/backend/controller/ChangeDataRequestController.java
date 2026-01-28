package com.tricolori.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.dto.profile.ChangeDataRequestResponse;
import com.tricolori.backend.dto.profile.ProfileRequest;
import com.tricolori.backend.entity.ChangeDataRequest;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.exception.DriverHasPendingProfileRequestException;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.mapper.ChangeDataRequestMapper;
import com.tricolori.backend.repository.DriverRepository;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.service.ChangeDataRequestService;
import com.tricolori.backend.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/change-requests")
public class ChangeDataRequestController {
    private final PersonRepository personRepository;
    private final DriverRepository driverRepository;

    private final ChangeDataRequestService service;
    private final ProfileService profileService;

    private final ChangeDataRequestMapper mapper;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChangeDataRequestResponse>> getAllPendingRequests() {
        List<ChangeDataRequestResponse> pendingRequests = service.getAllPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @PutMapping("/approve/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveRequest(@PathVariable Long requestId) {
        ChangeDataRequest request = service.getPendingById(requestId);
        Person user = personRepository.findById(request.getProfile().getId())
            .orElseThrow(() -> { throw new PersonNotFoundException("Driver not found"); });
        
        service.approve(request);
        profileService.updateMyProfile(user, mapper.toProfileRequest(request.getChanges()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reject/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long requestId) {
        ChangeDataRequest request = service.getPendingById(requestId);

        service.reject(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> requestChanges(
        @AuthenticationPrincipal Person user,
        @RequestBody ProfileRequest request
    ) {
        Person dbPerson = personRepository.findById(user.getId())
            .orElseThrow(() -> new PersonNotFoundException("Person not found"));

        Driver driver = driverRepository.findById(dbPerson.getId())
            .orElseThrow(() -> new PersonNotFoundException("Driver not found"));

        if (service.driverHasPendingRequest(driver)) {
            throw new DriverHasPendingProfileRequestException();
        }
        
        service.createRequest(driver, request);
        return ResponseEntity.ok().build();
    }
}
