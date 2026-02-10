package com.tricolori.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.repository.PassengerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerService {
    private final PassengerRepository repository;

    public List<Passenger> getTrackingPassengers(String[] emails) {
        if (emails == null || emails.length == 0) {
            return new ArrayList<>();
        }
        return repository.findAllByEmailIn(Arrays.asList(emails));
    }
}
