package com.tricolori.backend.repository;

import com.tricolori.backend.entity.TrackingToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrackingTokenRepository extends JpaRepository<TrackingToken, Long> {
    Optional<TrackingToken> findByToken(String token);
    Optional<TrackingToken> findByEmailAndRideId(String email, Long rideId);
    List<TrackingToken> findByRideId(Long rideId);
    List<TrackingToken> findByEmail(String email);
}