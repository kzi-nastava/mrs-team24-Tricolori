package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Passenger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    // Find passengers whose email is in 'emails' list:
    @Query("SELECT p FROM Passenger p WHERE p.email IN :emails")
    List<Passenger> findAllByEmailIn(@Param("emails") List<String> emails);
}
