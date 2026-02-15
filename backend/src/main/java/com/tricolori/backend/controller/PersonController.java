package com.tricolori.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.dto.block.ActivePersonStatus;
import com.tricolori.backend.dto.block.BlockRequest;
import com.tricolori.backend.service.PersonService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/persons")
public class PersonController {
    private final PersonService service;

    @PatchMapping("/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockUser(
        @Valid @RequestBody BlockRequest request
    ) {
        service.applyBlock(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockUser(
        @RequestParam @Email String email
    ) {
        service.removeBlock(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivePersonStatus>> getActivePersons(
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) @Email String email,
        @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        var page = service.getPersonsStatuses(id, firstName, lastName, email, pageable);
        return ResponseEntity.ok(page);
    }
}
