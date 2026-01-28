
package com.tricolori.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tricolori.backend.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    @Query("SELECT d FROM Driver d JOIN d.dailyLogs log " +
           "WHERE log.date = CURRENT_DATE " +
           "AND log.active = true")
    List<Driver> getAllCurrentlyActiveDrivers();
}
