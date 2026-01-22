package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
}
