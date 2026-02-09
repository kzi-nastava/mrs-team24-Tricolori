package com.tricolori.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.repository.PersonRepository;
import org.springframework.stereotype.Service;

import com.tricolori.backend.dto.profile.ChangeDataRequestResponse;
import com.tricolori.backend.dto.profile.ProfileRequest;
import com.tricolori.backend.entity.ChangeDataRequest;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.exception.ChangeRequestNotFoundException;
import com.tricolori.backend.mapper.ChangeDataRequestMapper;
import com.tricolori.backend.repository.ChangeDataRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangeDataRequestService {
    private final ChangeDataRequestRepository repository;
    private final ChangeDataRequestMapper mapper;
    private final NotificationService notificationService;
    private final PersonRepository personRepository;

    public void createRequest(Driver person, ProfileRequest request) {
        ChangeDataRequest changeRequest = new ChangeDataRequest();
        changeRequest.setChanges(mapper.fromProfileRequest(request));
        changeRequest.setApproved(false);
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setProfile(person);

        repository.save(changeRequest);

        // notify admin about new change request
        notificationService.sendProfileChangeRequestNotification(personRepository.findByRole(PersonRole.ROLE_ADMIN).getFirst().getEmail(),
                person.getFirstName() + " " + person.getLastName(),
                person.getId());
    }

    public void approve(ChangeDataRequest request) {
        request.setApproved(true);
        request.setReviewedAt(LocalDateTime.now());
        repository.save(request);
    }

    public void reject(ChangeDataRequest request) {
        request.setApproved(false);
        request.setReviewedAt(LocalDateTime.now());
        repository.save(request);
    }

    public boolean driverHasPendingRequest(Driver driver) {
        return repository.findAllPending().size() != 0;
    }

    public List<ChangeDataRequestResponse> getAllPendingRequests() {
        return repository.findAllPending().stream()
            .map(mapper::backToAdmin)
            .toList();
    }

    public ChangeDataRequest getPendingById(Long id) {
        ChangeDataRequest request = repository.findById(id)
            .orElseThrow(ChangeRequestNotFoundException::new);
        if(!request.isPending()) throw new ChangeRequestNotFoundException("Request is not pending");
        return request;
    }

    public Optional<ChangeDataRequest> getById(Long id) {
        return repository.findById(id);
    }
}
