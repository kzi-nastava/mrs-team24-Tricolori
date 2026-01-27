package com.tricolori.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.dto.profile.ProfileRequest;
import com.tricolori.backend.dto.profile.ProfileResponse;

import com.tricolori.backend.mapper.PersonMapper;

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
