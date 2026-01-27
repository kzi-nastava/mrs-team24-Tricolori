package com.tricolori.backend.core.services;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tricolori.backend.core.domain.models.Driver;
import com.tricolori.backend.core.domain.models.Location;
import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.DriverRepository;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.AllDriversReachedWorkingLimitException;
import com.tricolori.backend.core.exceptions.NoActiveDriversException;
import com.tricolori.backend.shared.enums.RideStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository repository;
    private final RideRepository rideRepository;

    public Driver findDriverForRide(Ride newRide) {
        List<Driver> activeDrivers = repository.getAllCurrentlyActiveDrivers();
        if (activeDrivers.isEmpty()) {
            throw new NoActiveDriversException();
        }

        List<Driver> driversBelowDailyLimit = activeDrivers.stream()
            .filter(this::isWorkTimeValid)
            .toList();

        if (driversBelowDailyLimit.isEmpty()) {
            throw new AllDriversReachedWorkingLimitException();
        }

        List<Driver> freeDrivers = getTrulyFreeDrivers(driversBelowDailyLimit);
        if (!freeDrivers.isEmpty()) {
            return findClosestToLocation(freeDrivers, newRide.getRoute().getPickupStop().getLocation());
        }

        Driver bestBusyDriver = findBestBusyDriver(driversBelowDailyLimit, newRide);

        if (bestBusyDriver != null) {
            return bestBusyDriver;
        }

        throw new NoActiveDriversException("There are no available drivers.");
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

    private Driver findBestBusyDriver(List<Driver> candidates, Ride newRide) {
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
                return Location.calculateDistance(currentRide.getRoute().getDestinationStop().getLocation(), 
                    newRide.getRoute().getPickupStop().getLocation());
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
        // TODO...
        return true; 
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
}