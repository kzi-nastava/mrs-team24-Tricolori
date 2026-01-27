package com.tricolori.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tricolori.backend.dto.profile.ProfileRequest;
import com.tricolori.backend.entity.ChangeDataRequest;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.mapper.ChangeDataRequestMapper;
import com.tricolori.backend.repository.ChangeDataRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangeDataRequestService {
    private final ChangeDataRequestRepository repository;
    private final ChangeDataRequestMapper mapper;

    public void createRequest(Driver person, ProfileRequest request) {
        ChangeDataRequest changeRequest = new ChangeDataRequest();
        changeRequest.setChanges(mapper.fromProfileRequest(request));
        changeRequest.setApproved(false);
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setProfile(person);

        repository.save(changeRequest);
    }

    public boolean driverHasPendingRequest(Driver driver) {
        return repository.findAllPending().size() != 0;
    }
}
