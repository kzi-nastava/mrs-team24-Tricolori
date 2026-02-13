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

import java.time.LocalDate;
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

    boolean existsByDriverIdAndStatus(Long driverId, RideStatus rideStatus);

    @Query("""
        SELECT r 
        FROM Ride r 
        WHERE r.driver.id = :driverId 
          AND r.status IN :statuses 
        ORDER BY r.createdAt DESC
    """)
    Optional<Ride> findRideByDriverAndStatuses(
            @Param("driverId") Long driverId,
            @Param("statuses") Collection<RideStatus> statuses
    );

    @Query("""
        SELECT r 
        FROM Ride r 
        JOIN r.passengers p 
        WHERE p.id = :passengerId 
          AND r.status IN :statuses 
        ORDER BY r.createdAt DESC
    """)
    Optional<Ride> findRideByPassengerAndStatuses(
            @Param("passengerId") Long passengerId,
            @Param("statuses") Collection<RideStatus> statuses
    );

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

    @Query("SELECT r FROM Ride r " +
            "JOIN r.passengers p " +
            "WHERE p.id = :passengerId " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR r.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR r.createdAt <= :endDate)")
    Page<Ride> findAllPassengerRides(
            @Param("passengerId") Long passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT DISTINCT r FROM Ride r " +
            "LEFT JOIN r.passengers p " +
            "WHERE (:personEmail IS NULL OR r.driver.email = :personEmail OR p.email = :personEmail) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR r.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR r.createdAt <= :endDate)")
    Page<Ride> findAdminRideHistory(
            @Param("personEmail") String personEmail,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

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

    // Find completed rides within a time window for rating reminders
    // Change from completedAt to endTime
    @Query("""
    SELECT r
    FROM Ride r
    WHERE r.status = 'COMPLETED'
      AND r.endTime >= :startWindow
      AND r.endTime <= :endWindow
""")
    List<Ride> findCompletedRidesBetween(
            @Param("startWindow") LocalDateTime startWindow,
            @Param("endWindow") LocalDateTime endWindow
    );

    // Find scheduled rides within a time window for ride reminders
    @Query("""
    SELECT r
    FROM Ride r
    WHERE r.status = 'SCHEDULED'
      AND r.scheduledFor >= :startWindow
      AND r.scheduledFor <= :endWindow
""")
    List<Ride> findScheduledRidesBetween(
            @Param("startWindow") LocalDateTime startWindow,
            @Param("endWindow") LocalDateTime endWindow
    );
}
