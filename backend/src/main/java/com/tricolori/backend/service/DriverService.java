package com.tricolori.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.tricolori.backend.exception.NoActiveDriversException;
import com.tricolori.backend.repository.DriverRepository;
import com.tricolori.backend.repository.RideRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository repository;
    private final RideRepository rideRepository;

    public Driver findDriverForRide(Location pickup, RidePreferences preferences) {
        List<Driver> activeDrivers = repository.getAllCurrentlyActiveDrivers();
        if (activeDrivers.isEmpty()) {
            throw new NoActiveDriversException("Trenutno nema aktivnih vozaca.");
        }

        // 2. Filtriraj vozače: Radno vreme + Specifikacija vozila (preferences)
        List<Driver> eligibleDrivers = activeDrivers.stream()
            .filter(this::isWorkTimeValid)
            .filter(d -> matchesPreferences(d, preferences)) // <-- NOVO
            .toList();

        if (eligibleDrivers.isEmpty()) {
            throw new NoActiveDriversException("Nema vozača koji ispunjavaju kriterijume (tip vozila/oprema).");
        }

        // 3. Traži potpuno slobodne vozače među onima koji ispunjavaju uslove
        List<Driver> freeDrivers = getTrulyFreeDrivers(eligibleDrivers);
        if (!freeDrivers.isEmpty()) {
            return findClosestToLocation(freeDrivers, pickup);
        }

        // 4. Ako su svi zauzeti, traži onoga koji završava uskoro (i ispunjava preferences)
        Driver bestBusyDriver = findBestBusyDriver(eligibleDrivers, pickup);

        if (bestBusyDriver != null) {
            return bestBusyDriver;
        }

        throw new NoActiveDriversException("Svi odgovarajući vozači su trenutno zauzeti.");
    }

    // Pomoćna metoda za proveru preferencija
    private boolean matchesPreferences(Driver driver, RidePreferences prefs) {
        VehicleSpecification spec = driver.getVehicle().getSpecification();

        if (spec.getType() != prefs.vehicleType()) {
            return false;
        }

        if (prefs.petFriendly() && !spec.isPetFriendly()) {
            return false;
        }

        if (prefs.babyFriendly() && !spec.isBabyFriendly()) {
            return false;
        }

        return true;
    }

    private List<Driver> getTrulyFreeDrivers(List<Driver> candidates) {
        List<Ride> busyRides = rideRepository.findAllByStatusIn(List.of(RideStatus.ONGOING, RideStatus.SCHEDULED));
        Set<Long> busyDriverIds = busyRides.stream()
            .filter(r -> r.getDriver() != null)
            .map(r -> r.getDriver().getId())
            .collect(Collectors.toSet());

        return candidates.stream()
            .filter(d -> !busyDriverIds.contains(d.getId()))
            .toList();
    }

    private Driver findBestBusyDriver(List<Driver> candidates, Location pickup) {
        return candidates.stream()
            .filter(d -> {
                List<Ride> driverRides = rideRepository.findAllByDriverAndStatusIn(d, 
                        List.of(RideStatus.ONGOING, RideStatus.SCHEDULED));
                
                boolean hasScheduled = driverRides.stream().anyMatch(r -> r.getStatus() == RideStatus.SCHEDULED);
                if (hasScheduled) return false;

                return driverRides.stream()
                        .filter(r -> r.getStatus() == RideStatus.ONGOING)
                        .anyMatch(this::isEndingInTenMinutes);
            })
            .min(Comparator.comparingDouble(d -> {
                Ride currentRide = getCurrentOngoingRide(d);
                return Location.calculateDistance(currentRide.getRoute().getDestinationStop().getLocation(), pickup);
            }))
            .orElse(null);
    }

    private boolean isWorkTimeValid(Driver driver) {
        return driver.getDailyLogs().stream()
            .filter(log -> log.getDate().equals(LocalDate.now()))
            .findFirst()
            .map(log -> log.getActiveTimeSeconds() < 28800)
            .orElse(false);
    }

    private boolean isEndingInTenMinutes(Ride ride) {
        if (ride.getStartTime() == null || ride.getRoute().getEstimatedTimeSeconds() == null) {
            return false;
        }

        LocalDateTime estimatedArrivalTime = ride.getStartTime()
                .plusSeconds(ride.getRoute().getEstimatedTimeSeconds());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesFromNow = now.plusMinutes(10);

        return estimatedArrivalTime.isBefore(tenMinutesFromNow);
    }

    private Driver findClosestToLocation(List<Driver> drivers, Location pickupLoc) {
        return drivers.stream()
            .min(Comparator.comparingDouble(d -> 
                Location.calculateDistance(d.getVehicle().getLocation(), pickupLoc)
            ))
            .orElse(null);
    }

    private Ride getCurrentOngoingRide(Driver d) {
        return rideRepository.findAllByDriverAndStatusIn(d, List.of(RideStatus.ONGOING)).get(0);
    }

    public Driver findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}