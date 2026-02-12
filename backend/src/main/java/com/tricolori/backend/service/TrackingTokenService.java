package com.tricolori.backend.service;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.entity.TrackingToken;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.TrackingTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingTokenService {

    private final TrackingTokenRepository trackingTokenRepository;
    private final PersonRepository personRepository;

    @Transactional
    public String createTrackingToken(String email, String firstName, Ride ride) {
        return createTrackingToken(email, firstName, null, ride);
    }

    @Transactional
    public String createTrackingToken(String email, String firstName, String lastName, Ride ride) {
        // Check if token already exists for this email and ride
        return trackingTokenRepository.findByEmailAndRideId(email, ride.getId())
                .map(TrackingToken::getToken)
                .orElseGet(() -> {
                    String token = UUID.randomUUID().toString();
                    TrackingToken trackingToken = new TrackingToken(token, email, firstName, lastName, ride);

                    // If this is a registered user, link their Person entity
                    Optional<Person> person = personRepository.findByEmail(email);
                    person.ifPresent(trackingToken::setPerson);

                    trackingTokenRepository.save(trackingToken);
                    log.info("Created tracking token for email: {} (registered: {}) and ride: {}",
                            email, person.isPresent(), ride.getId());
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

    public java.util.List<TrackingToken> getTokensForRide(Long rideId) {
        return trackingTokenRepository.findByRideId(rideId);
    }
}