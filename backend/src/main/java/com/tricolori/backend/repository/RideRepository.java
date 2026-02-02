package com.tricolori.backend.repository;


import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.RideStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    // Find rides where the passenger is in the passengers list
    @Query("SELECT r FROM Ride r JOIN r.passengers p WHERE p.id = :passengerId ORDER BY r.createdAt DESC")
    List<Ride> findByPassengerIdOrderByCreatedAtDesc(@Param("passengerId") Long passengerId);

    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Ride> findByStatus(RideStatus status);

    List<Ride> findAllByDriverAndStatusIn(Driver driver, Collection<RideStatus> statuses);

    List<Ride> findAllByStatusIn(Collection<RideStatus> statuses);
    
    // current ride for driver
    @Query("""
        SELECT r
        FROM Ride r
        WHERE r.driver.id = :driverId
          AND r.status IN ('ACCEPTED', 'IN_PROGRESS')
        ORDER BY r.createdAt DESC
    """)
    Optional<Ride> findCurrentRideByDriver(@Param("driverId") Long driverId);

    // current ride for passenger
    @Query("""
        SELECT r
        FROM Ride r
        JOIN r.passengers p
        WHERE p.id = :passengerId
          AND r.status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS')
        ORDER BY r.createdAt DESC
    """)
    Optional<Ride> findCurrentRideByPassenger(@Param("passengerId") Long passengerId);

    @Query("""
    SELECT r
    FROM Ride r
    WHERE r.driver.id = :driverId
      AND r.status = 'ONGOING'
    """)
    Optional<Ride> findOngoingRideByDriver(@Param("driverId") Long driverId);

    @Query("""
    SELECT r
    FROM Ride r
    JOIN r.passengers p
    WHERE p.id = :passengerId
      AND r.status = 'ONGOING'
    """)
    Optional<Ride> findOngoingRideByPassenger(@Param("passengerId") Long passengerId);

    // all driver rides
    @Query("""
        SELECT r
        FROM Ride r
        WHERE r.driver.id = :driverId
    """)
    Page<Ride> findAllDriverRides(
            @Param("driverId") Long driverId,
            Pageable pageable
    );

    // driver rides filtered by date
    @Query("""
        SELECT r
        FROM Ride r
        WHERE r.driver.id = :driverId
          AND r.createdAt >= :startDate
          AND r.createdAt <= :endDate
    """)
    Page<Ride> findDriverRidesByDateRange(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // all passenger rides
    @Query("SELECT r FROM Ride r " +
            "JOIN r.passengers p " +
            "WHERE p.id = :passengerId " +
            "ORDER BY r.createdAt DESC")
    Page<Ride> findAllPassengerRides(@Param("passengerId") Long passengerId, Pageable pageable);

    // passenger rides filtered by date
    @Query("""
        SELECT DISTINCT r
        FROM Ride r
        JOIN r.passengers p
        WHERE p.id = :passengerId
          AND r.createdAt >= :startDate
          AND r.createdAt <= :endDate
    """)
    Page<Ride> findPassengerRidesByDateRange(
            @Param("passengerId") Long passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
