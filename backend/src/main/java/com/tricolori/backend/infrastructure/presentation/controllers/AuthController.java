package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.core.services.AuthService;
import com.tricolori.backend.infrastructure.presentation.dtos.ForgotPasswordRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.RegisterDriverRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import com.tricolori.backend.infrastructure.presentation.dtos.RegisterPassengerRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.ResetPasswordRequest;
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

        String token = "dummy-jwt-token";
        LoginResponse response = new LoginResponse(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-driver")
    public ResponseEntity<Void> registerDriver(@Valid @RequestBody RegisterDriverRequest request) {
      
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
      
    @PostMapping(path = "/register-passenger", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> register(
            @Valid @RequestPart("data") RegisterPassengerRequest request,
            @RequestPart("image") MultipartFile pfp
    ) {

        authService.registerPassenger(request, pfp);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
