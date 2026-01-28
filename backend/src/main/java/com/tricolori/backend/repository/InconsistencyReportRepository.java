package com.tricolori.backend.repository;

import com.tricolori.backend.entity.InconsistencyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InconsistencyReportRepository
        extends JpaRepository<InconsistencyReport, Long> {

    // all reports for a ride
    List<InconsistencyReport> findAllByRideId(Long rideId);

    // all reports created by a passenger
    List<InconsistencyReport> findAllByReporterId(Long reporterId);
    // all reports for rides where the driver has the given id
    List<InconsistencyReport> findAllByRideDriverId(Long driverId);
}
