package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.shared.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    // Find rides where the passenger is in the passengers list
    @Query("SELECT r FROM Ride r JOIN r.passengers p WHERE p.id = :passengerId ORDER BY r.createdAt DESC")
    List<Ride> findByPassengerIdOrderByCreatedAtDesc(@Param("passengerId") Long passengerId);

    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Ride> findByStatus(RideStatus status);

    @Query("SELECT r FROM Ride r WHERE r.driver.id = :driverId AND r.status IN ('ACCEPTED', 'IN_PROGRESS') ORDER BY r.createdAt DESC")
    Optional<Ride> findCurrentRideByDriver(@Param("driverId") Long driverId);

    // Updated to use JOIN with passengers collection
    @Query("SELECT r FROM Ride r JOIN r.passengers p WHERE p.id = :passengerId AND r.status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS') ORDER BY r.createdAt DESC")
    Optional<Ride> findCurrentRideByPassenger(@Param("passengerId") Long passengerId);

    @Query("SELECT r FROM Ride r WHERE r.driver.id = :driverId  ORDER BY r.createdAt DESC")
    List<Ride> findAllByDriverId (
            @Param("driverId") Long driverId
    );

    @Query("SELECT r FROM Ride r WHERE r.driver.id = :driverId AND (:startDate IS NULL OR DATE(r.createdAt) >= :startDate) AND (:endDate IS NULL OR DATE(r.createdAt) <= :endDate) ORDER BY r.createdAt DESC")
    List<Ride> findDriverRideHistory(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Fixed query with proper JOIN and parameter name
    @Query("SELECT DISTINCT r FROM Ride r " +
            "LEFT JOIN r.passengers p " +
            "WHERE " +
            "  (:passengerId IS NULL OR p.id = :passengerId) " +
            "  AND (:driverId IS NULL OR r.driver.id = :driverId) " +
            "  AND (:status IS NULL OR r.status = :status) " +
            "  AND (:startDate IS NULL OR DATE(r.createdAt) >= :startDate) " +
            "  AND (:endDate IS NULL OR DATE(r.createdAt) <= :endDate)")
    Page<Ride> findRideHistoryWithFilters(
            @Param("passengerId") Long passengerId,
            @Param("driverId") Long driverId,
            @Param("status") RideStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
