package com.tricolori.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.dto.report.AdminReportRequest;
import com.tricolori.backend.dto.report.ReportRequest;
import com.tricolori.backend.dto.report.ReportResponse;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService service;
    
    @GetMapping("/personal")
    public ResponseEntity<ReportResponse> getPersonalReport(
        @AuthenticationPrincipal Person person,
        ReportRequest request
    ) {
        var response = service.getPersonalResponse(person, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<ReportResponse> getAdminReport(
        @AuthenticationPrincipal Person person,
        AdminReportRequest request
    ) {
        var response = service.getAdminResponse(person, request);
        return ResponseEntity.ok(response);
    }
}
