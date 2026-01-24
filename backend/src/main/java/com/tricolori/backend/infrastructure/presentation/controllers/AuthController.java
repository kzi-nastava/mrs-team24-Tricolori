package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.core.services.AuthService;
import com.tricolori.backend.infrastructure.presentation.dtos.ForgotPasswordRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import com.tricolori.backend.infrastructure.presentation.dtos.RegisterPassengerRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.ResetPasswordRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.Auth.AdminDriverRegistrationRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(path = "/register-driver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerDriver(
        @Valid @RequestPart("dataRequest") AdminDriverRegistrationRequest request,
        @RequestPart(value = "pfpFile", required = false) MultipartFile pfp
    ) {
        authService.registerDriver(request, pfp);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Successfully registered a new driver. Registration's final step will be sent to driver's email.");
    }

    @PostMapping(path = "/register-passenger", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> register(
            @Valid @RequestPart("data") RegisterPassengerRequest request,
            @RequestPart(value = "image", required = false) MultipartFile pfp
    ) {

        authService.registerPassenger(request, pfp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Please check your email to activate your account.");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam("token") String token) {

        authService.activateAccount(token);
        return ResponseEntity.ok("Account activated successfully! You can now login.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok().build();
    }
}
