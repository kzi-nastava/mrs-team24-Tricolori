package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

}
