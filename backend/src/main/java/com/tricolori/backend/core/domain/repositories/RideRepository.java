package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository <Ride, Long> {
}
