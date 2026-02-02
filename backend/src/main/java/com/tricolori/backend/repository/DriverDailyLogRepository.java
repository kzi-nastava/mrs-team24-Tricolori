package com.tricolori.backend.repository;

import com.tricolori.backend.entity.DriverDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverDailyLogRepository extends JpaRepository<DriverDailyLog, Long> {

    List<DriverDailyLog> findByDriverId(Long driverId);

    Optional<DriverDailyLog> findByDriverIdAndDate(Long driverId, LocalDate date);
}
