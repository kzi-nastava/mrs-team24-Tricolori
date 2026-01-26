package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.InconsistencyReport;
import com.tricolori.backend.core.domain.models.Passenger;
import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.InconsistencyReportRepository;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.RideNotFoundException;
import com.tricolori.backend.infrastructure.presentation.dtos.InconsistencyReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InconsistencyReportService {

    private final InconsistencyReportRepository inconsistencyReportRepository;
    private final RideRepository rideRepository;

    // ================= create =================

    @Transactional
    public void reportInconsistency(
            Long rideId,
            Long passengerId,
            InconsistencyReportRequest request
    ) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        Passenger passenger = ride.getPassengers().stream()
                .filter(p -> p.getId().equals(passengerId))
                .findFirst()
                .orElseThrow(() ->
                        new AccessDeniedException("not a passenger of this ride")
                );

        InconsistencyReport report = new InconsistencyReport();
        report.setRide(ride);
        report.setReporter(passenger);
        report.setDescription(request.getDescription());

        inconsistencyReportRepository.save(report);
    }

    // ================= queries =================

    public List<InconsistencyReport> getReportsForRide(Long rideId) {
        return inconsistencyReportRepository.findAllByRideId(rideId);
    }

    public List<InconsistencyReport> getReportsForDriver(Long driverId) {
        return inconsistencyReportRepository.findAllByRide_Driver_Id(driverId);
    }

    public List<InconsistencyReport> getReportsByPassenger(Long passengerId) {
        return inconsistencyReportRepository.findAllByReporterId(passengerId);
    }
}
