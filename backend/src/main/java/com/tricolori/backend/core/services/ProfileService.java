package com.tricolori.backend.core.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.core.exceptions.PersonNotFoundException;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileResponse;

import com.tricolori.backend.infrastructure.presentation.mappers.PersonMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Transactional
    public ProfileResponse updateMyProfile(Person currentPerson, ProfileRequest request) {
        Person dbPerson = personRepository.findById(currentPerson.getId())
            .orElseThrow(() -> new PersonNotFoundException("Person not found"));

        // Update fields based on request:
        dbPerson.setFirstName(request.getFirstName());
        dbPerson.setLastName(request.getLastName());
        dbPerson.setHomeAddress(request.getHomeAddress());
        dbPerson.setPhoneNum(request.getPhoneNumber());
        dbPerson.setPfpUrl(request.getPfp());

        // Save changes to DB:
        Person savedPerson = personRepository.save(dbPerson);
        return personMapper.toProfileResponse(savedPerson);
    }
}
