package com.tricolori.backend.service;

import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.entity.TrackingToken;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.TrackingTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingTokenService {

    private final TrackingTokenRepository trackingTokenRepository;
    private final PersonRepository personRepository;

    @Transactional
    public String createTrackingToken(String email, Ride ride) {
        // Check if token already exists for this email and ride
        return trackingTokenRepository.findByEmailAndRideId(email, ride.getId())
                .map(TrackingToken::getToken)
                .orElseGet(() -> {
                    String token = UUID.randomUUID().toString();
                    TrackingToken trackingToken = new TrackingToken(token, email, ride);
                    trackingTokenRepository.save(trackingToken);
                    log.info("Created tracking token for email: {} and ride: {}", email, ride.getId());
                    return token;
                });
    }

    public TrackingToken validateToken(String token) {
        TrackingToken trackingToken = trackingTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid tracking token"));

        if (trackingToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Tracking token has expired");
        }

        return trackingToken;
    }

    public boolean isRegisteredUser(String email) {
        return personRepository.findByEmail(email).isPresent();
    }
}