package com.tricolori.backend.controller;

import com.tricolori.backend.dto.profile.ChangeDriverStatusRequest;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.service.DriverDailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/driver-daily-logs")
@RequiredArgsConstructor
public class DriverDailyLogController {

    private final DriverDailyLogService dailyLogService;

    @PatchMapping("/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> changeStatus(
            @RequestBody ChangeDriverStatusRequest request,
            @AuthenticationPrincipal Person driver
    ) {

        dailyLogService.changeStatus(request, driver.getId());
        return ResponseEntity.ok().build();
    }


}
