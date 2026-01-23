package com.tricolori.backend.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileResponse;

@Service
public class ProfileService {
    @Autowired
    private PersonRepository personRepository;

    @Transactional
    public ProfileResponse updateMyProfile(Person currentPerson, ProfileRequest request) {
        // TODO: add custom exception...
        Person dbPerson = personRepository.findById(currentPerson.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields based on request:
        dbPerson.setFirstName(request.getFirstName());
        dbPerson.setLastName(request.getLastName());
        dbPerson.setHomeAddress(request.getHomeAddress());
        dbPerson.setPhoneNum(request.getPhoneNumber());
        dbPerson.setPfpUrl(request.getPfp());

        // Save changes to DB:
        Person savedPerson = personRepository.save(dbPerson);
        return ProfileResponse.fromPerson(savedPerson);
    }
}
