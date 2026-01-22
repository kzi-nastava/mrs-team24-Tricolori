package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByAvailableTrue();

    Optional<Vehicle> findByPlateNum(String plateNum);

    List<Vehicle> findBySpecificationId(Long specificationId);

    @Query("SELECT d.vehicle FROM Driver d WHERE d.id = :driverId")
    Optional<Vehicle> findByDriverId(@Param("driverId") Long driverId);

    @Query("SELECT v FROM Vehicle v WHERE v.available = true AND v.latitude IS NOT NULL AND v.longitude IS NOT NULL")
    List<Vehicle> findAllActiveWithLocation();
}