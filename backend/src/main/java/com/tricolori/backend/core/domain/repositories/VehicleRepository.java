package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByActiveTrue();

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByDriverId(Long driverId);

    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.currentLatitude IS NOT NULL AND v.currentLongitude IS NOT NULL")
    List<Vehicle> findAllActiveWithLocation();
}