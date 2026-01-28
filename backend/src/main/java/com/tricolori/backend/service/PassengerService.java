package com.tricolori.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.repository.PassengerRepository;
import com.tricolori.backend.repository.PersonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerService {
    private final PassengerRepository repository;
    private final PersonRepository personRepository;

    public List<Passenger> getTrackingPassengers(String[] emails) {
        List<Passenger> trackers = new ArrayList<>();

        for(String email : emails) {
            try {
                Person person = personRepository.findByEmail(email)
                    .orElseThrow();
                
                Passenger tracker = repository.findById(person.getId())
                    .orElseThrow();
                
                trackers.add(tracker);
            }
            catch(Exception e) {}
        }

        return trackers;
    }
}
