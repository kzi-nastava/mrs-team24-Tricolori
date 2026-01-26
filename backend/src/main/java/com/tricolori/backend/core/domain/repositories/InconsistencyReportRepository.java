package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.InconsistencyReport;
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

    List<InconsistencyReport> findAllByRide_Driver_Id(Long driverId);
}
