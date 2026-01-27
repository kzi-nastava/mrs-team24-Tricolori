package com.tricolori.backend.controller;

import com.tricolori.backend.dto.profile.ChangeDriverStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@RequestBody ChangeDriverStatusRequest request, @PathVariable Long id) {

        return ResponseEntity.ok().build();
    }
}
