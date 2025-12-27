package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.LoginRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.RegisterDriverRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

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
}
