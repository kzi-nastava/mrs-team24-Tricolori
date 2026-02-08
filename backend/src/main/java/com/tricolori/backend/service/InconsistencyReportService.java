package com.tricolori.backend.service;

import com.tricolori.backend.entity.InconsistencyReport;
import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.repository.InconsistencyReportRepository;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.dto.ride.InconsistencyReportRequest;
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
    private final NotificationService notificationService;
    private final PersonRepository personRepository;

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

        String adminEmail = personRepository.findByRole(PersonRole.ROLE_ADMIN)
                .stream()
                .findFirst()
                .map(Person::getEmail)
                .orElseThrow(() -> new RuntimeException("admin user not found"));
        notificationService.sendRideReportNotification(adminEmail, rideId, "Route inconsistency", request.getDescription());
        inconsistencyReportRepository.save(report);
    }

    // ================= queries =================

    public List<InconsistencyReport> getReportsForRide(Long rideId) {
        return inconsistencyReportRepository.findAllByRideId(rideId);
    }

    public List<InconsistencyReport> getReportsForDriver(Long driverId) {
        return inconsistencyReportRepository.findAllByRideDriverId(driverId);
    }

    public List<InconsistencyReport> getReportsByPassenger(Long passengerId) {
        return inconsistencyReportRepository.findAllByReporterId(passengerId);
    }
}
