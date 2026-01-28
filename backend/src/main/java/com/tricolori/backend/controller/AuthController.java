package com.tricolori.backend.controller;

import com.tricolori.backend.service.AuthService;
import com.tricolori.backend.dto.auth.ForgotPasswordRequest;
import com.tricolori.backend.dto.auth.LoginRequest;
import com.tricolori.backend.dto.auth.LoginResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import com.tricolori.backend.dto.auth.RegisterPassengerRequest;
import com.tricolori.backend.dto.auth.ResetPasswordRequest;
import com.tricolori.backend.dto.auth.AdminDriverRegistrationRequest;
import com.tricolori.backend.dto.auth.DriverPasswordSetupRequest;
import com.tricolori.backend.enums.RegistrationTokenVerificationStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerDriver(
        @Valid @RequestPart("data") AdminDriverRegistrationRequest request,
        @RequestPart(value = "image", required = false) MultipartFile pfp
    ) {
        authService.registerDriver(request, pfp);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Successfully registered a new driver. Registration's final step will be sent to driver's email.");
    }



    @PostMapping("/driver-activate")
    public ResponseEntity<String> driverPasswordSetup(
        @Valid @RequestBody DriverPasswordSetupRequest request
    ) {
        authService.driverPasswordSetup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Driver registration completed.");
    }

    @GetMapping("/verify-token/{token}")
    public ResponseEntity<String> verifyToken(@PathVariable String token) {
        RegistrationTokenVerificationStatus status = authService.verifyToken(token);
        
        return ResponseEntity.ok(status.toString());
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
