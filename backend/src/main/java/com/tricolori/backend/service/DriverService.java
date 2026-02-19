package com.tricolori.backend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tricolori.backend.dto.ride.RidePreferences;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.entity.VehicleSpecification;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.exception.NoFreeDriverCloseException;
import com.tricolori.backend.exception.NoSuitableDriversException;
import com.tricolori.backend.repository.DriverRepository;
import com.tricolori.backend.repository.RideRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository repository;
    private final RideRepository rideRepository;

    public Driver findDriverForRide(
        Location pickup, 
        RidePreferences preferences, 
        int trackingPassengersNumber
    ) {
        List<Driver> activeDrivers = repository.getAllCurrentlyActiveDrivers();
        List<Driver> eligibleDrivers = activeDrivers.stream()
            .filter(this::isWorkTimeValid)
            .filter(d -> matchesPreferences(d, preferences, trackingPassengersNumber))
            .toList();

        if (eligibleDrivers.isEmpty()) {
            throw new NoSuitableDriversException("Nema aktivnih vozaca koji ispunjavaju kriterijume vozila.");
        }

        LocalDateTime scheduleFor = preferences.scheduledFor();
        if (scheduleFor != null) {
            eligibleDrivers = eligibleDrivers.stream()
                .filter(driver -> !isDriverBusyAt(driver, scheduleFor))
                .toList();
            
            if (eligibleDrivers.isEmpty())
                throw new NoSuitableDriversException("Svi odgovarajuci vozaci su zauzeti u zakazanom periodu.");
        }

        List<Driver> freeDrivers = getTrulyFreeDrivers(eligibleDrivers);
        if (!freeDrivers.isEmpty()) {
            Driver closest = findClosestToLocation(freeDrivers, pickup);
            if (closest == null)
                throw new NoFreeDriverCloseException("Greska pri odabiru najblizeg slobodnog vozaca."); 
            return closest;
        }

        Driver bestBusyDriver = findBestBusyDriver(eligibleDrivers, pickup);
        if (bestBusyDriver != null) {
            return bestBusyDriver;
        }

        throw new NoSuitableDriversException("Trenutno nema slobodnih vozača.");
    }

    private boolean isDriverBusyAt(Driver driver, LocalDateTime requestedTime) {
        List<Ride> driverRides = rideRepository.findAllByDriverAndStatusIn(
            driver, 
            List.of(RideStatus.ONGOING, RideStatus.SCHEDULED)
        );

        return driverRides.stream().anyMatch(ride -> {
            LocalDateTime start;
            if (ride.isOngoing()) {
                start = ride.getStartTime();     
            } else if (ride.isScheduledForLater()) {
                start = ride.getScheduledFor();
            } else {
                // We are waiting for driver to start this ride...
                start = ride.getCreatedAt();
            }

            LocalDateTime end = start.plusSeconds(ride.getRoute().getEstimatedTimeSeconds());

            return !requestedTime.isBefore(start.minusMinutes(5)) && !requestedTime.isAfter(end.plusMinutes(5));
        });
    }

    private List<Driver> getTrulyFreeDrivers(List<Driver> candidates) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // 2026-02-17T00:00:00
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX); // 2026-02-17T23:59:59.999...

        Set<Long> busyDriverIds = rideRepository.findAllByStatusInAndCreatedAtBetween(
                List.of(RideStatus.ONGOING, RideStatus.SCHEDULED),
                startOfDay,
                endOfDay
            )
            .stream()
            .filter(r -> r.getDriver() != null)
            .map(r -> r.getDriver().getId())
            .collect(Collectors.toSet());

        return candidates.stream()
            .filter(d -> !busyDriverIds.contains(d.getId()))
            .toList();
    }

    private boolean isEndingInTenMinutes(Ride ride) {
        if (ride.getStartTime() == null || ride.getRoute() == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime estimatedEnd = ride.getStartTime().plusSeconds(ride.getRoute().getEstimatedTimeSeconds());
        
        long minutesRemaining = Duration.between(now, estimatedEnd).toMinutes();
        
        return minutesRemaining >= 0 && minutesRemaining <= 10;
    }

    private boolean matchesPreferences(Driver driver, RidePreferences prefs, int trackingPassengersNumber) {
        VehicleSpecification spec = driver.getVehicle().getSpecification();
        return spec.getType() == prefs.vehicleType() &&
            (!prefs.petFriendly() || spec.isPetFriendly()) &&
            (!prefs.babyFriendly() || spec.isBabyFriendly()) &&
            spec.getNumSeats() >= trackingPassengersNumber;
    }

    private Driver findClosestToLocation(List<Driver> drivers, Location pickupLoc) {
        return drivers.stream()
            .min(Comparator.comparingDouble(d -> 
                Location.calculateDistance(d.getVehicle().getLocation(), pickupLoc)))
            .orElse(null);
    }

    private Driver findBestBusyDriver(List<Driver> candidates, Location pickup) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // 2026-02-17T00:00:00
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX); // 2026-02-17T23:59:59.999...

        return candidates.stream()
            .filter(d -> {
                List<Ride> driverRides = rideRepository.findAllByDriverAndStatusInAndCreatedAtBetween(
                    d, List.of(RideStatus.ONGOING, RideStatus.SCHEDULED), startOfDay, endOfDay
                );
                
                boolean hasScheduled = driverRides.stream().anyMatch(r -> r.getStatus() == RideStatus.SCHEDULED);
                if (hasScheduled) return false;

                return driverRides.stream()
                        .filter(r -> r.getStatus() == RideStatus.ONGOING)
                        .anyMatch(this::isEndingInTenMinutes);
            })
            .min(Comparator.comparingDouble(d -> {
                Ride currentRide = getCurrentOngoingRide(d);
                if (currentRide == null || currentRide.getRoute() == null) {
                    return Double.MAX_VALUE;
                }

                return Location.calculateDistance(currentRide.getRoute().getDestinationStop().getLocation(), pickup);
            }))
            .orElse(null);
    }

    private boolean isWorkTimeValid(Driver driver) {
        Long workTimeSeconds = 8L * 60L * 60L;
        return driver.getDailyLogs().stream()
            .filter(log -> log.getDate().equals(LocalDate.now()))
            .findFirst()
            .map(log -> log.getActiveTimeSeconds() < workTimeSeconds)
            .orElse(false);
    }

    private Ride getCurrentOngoingRide(Driver d) {
        return rideRepository.findAllByDriverAndStatusIn(d, List.of(RideStatus.ONGOING))
            .stream()
            .findFirst()
            .orElse(null);
    }

    public Driver findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
