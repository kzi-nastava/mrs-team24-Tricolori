package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Vehicle;
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

    @Query("SELECT v FROM Vehicle v WHERE v.available = true " +
            "AND v.location.latitude IS NOT NULL " +
            "AND v.location.longitude IS NOT NULL")
    List<Vehicle> findAllActiveWithLocation();

    @Query("SELECT v FROM Vehicle v WHERE " +
            "v.location.latitude IS NOT NULL " +
            "AND v.location.longitude IS NOT NULL")
    List<Vehicle> findAllWithLocation();
}