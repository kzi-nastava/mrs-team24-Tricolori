package com.tricolori.backend.service;

import com.tricolori.backend.dto.profile.DriverDto;
import com.tricolori.backend.dto.profile.PassengerDto;
import com.tricolori.backend.mapper.PersonMapper;
import com.tricolori.backend.mapper.RouteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import com.tricolori.backend.dto.ride.*;
import com.tricolori.backend.repository.PanicRepository;
import com.tricolori.backend.repository.PassengerRepository;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.repository.VehicleSpecificationRepository;
import com.tricolori.backend.entity.*;
import com.tricolori.backend.exception.CancelRideExpiredException;
import com.tricolori.backend.exception.ForeignRideException;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.exception.RideAlreadyStartedException;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;
import com.tricolori.backend.mapper.RideMapper;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;
    private final PersonRepository personRepository;
    private final PassengerRepository passengerRepository;
    private final PanicRepository panicRepository;
    private final OSRMService osrmService;

    private final PassengerService passengerService;
    private final DriverService driverService;
    
    private PriceList priceList;

    private final VehicleSpecificationRepository vehicleSpecificationRepository;
    private final RideMapper rideMapper;
    private final PersonMapper personMapper;
    private final RouteMapper routeMapper;
    private final ReviewService reviewService;
    private final PriceListService priceListService;
    private final RouteService routeService;
    private final InconsistencyReportService inconsistencyReportService;

    // ================= driver =================

    public Page<RideHistoryResponse> getDriverHistory(
            Long driverId,
            Pageable pageable
    ) {
        return rideRepository
                .findAllDriverRides(driverId, pageable)
                .map(rideMapper::toDriverHistoryResponse);
    }

    public RideDetailResponse getDriverRideDetail(
            Long rideId,
            Long driverId
    ) {
        Ride ride = getRideOrThrow(rideId);
        validateDriverAccess(ride, driverId);

        RideDetailResponse response =
                rideMapper.toDriverDetailResponse(ride);

        response.setDriverRating(
                round(reviewService.getAverageDriverRating(rideId))
        );
        response.setVehicleRating(
                round(reviewService.getAverageVehicleRating(rideId))
        );

        return response;
    }

    @Transactional
    public void completeRide(Long rideId, Long driverId) {
        Ride ride = getRideOrThrow(rideId);

        // security check
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("not authorized to complete this ride");
        }

        // state validation (optional but recommended)
        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new IllegalStateException("Ride is not in progress");
        }

        // complete ride
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());

        // calculate final price
        ride.setPrice(calculatePrice(ride));

        rideRepository.save(ride);
    }

    // ================= passenger =================

    public Page<PassengerRideHistoryResponse> getPassengerHistory(
            Long passengerId,
            Pageable pageable
    ) {
        return rideRepository
                .findAllPassengerRides(passengerId, pageable)
                .map(rideMapper::toPassengerHistoryResponse);
    }

    public PassengerRideDetailResponse getPassengerRideDetail(
            Long rideId,
            Long passengerId
    ) {
        Ride ride = getRideOrThrow(rideId);
        validatePassengerAccess(ride, passengerId);

        PassengerRideDetailResponse response =
                rideMapper.toPassengerDetailResponse(ride);

        response.setDriverRating(
                round(reviewService.getAverageDriverRating(rideId))
        );
        response.setVehicleRating(
                round(reviewService.getAverageVehicleRating(rideId))
        );

        return response;
    }

    public RideTrackingResponse trackRide(Long rideId) {
        Ride ride = getRideOrThrow(rideId);

        Integer estimatedMinutes =
                ride.getRoute() != null && ride.getRoute().getEstimatedTimeSeconds() != null
                        ? (int) Math.round(ride.getRoute().getEstimatedTimeSeconds() / 60.0)
                        : null;

        LocalDateTime estimatedArrival =
                ride.getStartTime() != null && estimatedMinutes != null
                        ? ride.getStartTime().plusMinutes(estimatedMinutes)
                        : null;

        VehicleLocationResponse currentLocation =
                ride.getDriver() != null && ride.getDriver().getVehicle() != null && ride.getDriver().getVehicle().getLocation() != null
                        ? new VehicleLocationResponse(
                        ride.getDriver().getVehicle().getId(),
                        ride.getDriver().getVehicle().getModel(),
                        ride.getDriver().getVehicle().getPlateNum(),
                        ride.getDriver().getVehicle().getLocation().getLatitude(),
                        ride.getDriver().getVehicle().getLocation().getLongitude(),
                        ride.getDriver().getVehicle().isAvailable()
                )
                        : null;

        // Map route to DetailedRouteResponse
        DetailedRouteResponse routeResponse =
                ride.getRoute() != null
                        ? routeMapper.toDetailedRoute(ride.getRoute())
                        : null;

        // Map driver to DTO
        DriverDto driverDto = null;
        if (ride.getDriver() != null) {
            Double rating = null;
            try {
                rating = reviewService.getAverageDriverRating(ride.getDriver().getId());
            } catch (Exception ignored) {}

            driverDto = personMapper.toDriverDto(ride.getDriver(), rating);
        }

        // Map passengers to DTOs
        List<PassengerDto> passengerDtos = null;

        if (ride.getPassengers() != null && !ride.getPassengers().isEmpty()) {
            Long mainId = ride.getPassengers().get(0).getId();

            passengerDtos = ride.getPassengers().stream()
                    .map(p -> {
                        PassengerDto dto = personMapper.toPassengerDto(p);
                        dto.setMainPassenger(p.getId().equals(mainId));
                        return dto;
                    })
                    .toList();
        }

        return new RideTrackingResponse(
                ride.getId(),
                ride.getStatus().name(),
                currentLocation,
                routeResponse,
                estimatedMinutes,
                estimatedArrival,
                ride.getScheduledFor(),
                ride.getStartTime(),
                ride.getPrice(),
                driverDto,
                passengerDtos
        );
    }

    @Transactional
    public Ride createRide(CreateRideRequest request, Long passengerId) {
        Person passenger = personRepository.findById(passengerId)
                .orElseThrow(() -> new PersonNotFoundException("Passenger not found"));

        List<Stop> stops = new ArrayList<>();

        stops.add(new Stop(
                request.getPickupAddress(),
                new Location(request.getPickupLongitude(), request.getPickupLatitude())
        ));

        if (request.getStops() != null && !request.getStops().isEmpty()) {
            for (StopDto stopReq : request.getStops()) {
                stops.add(new Stop(
                        stopReq.getAddress(),
                        new Location(stopReq.getLongitude(), stopReq.getLatitude())
                ));
            }
        }

        stops.add(new Stop(
                request.getDestinationAddress(),
                new Location(request.getDestinationLongitude(), request.getDestinationLatitude())
        ));

        Route route = routeService.findOrCreateRoute(stops);

        // hard coded id 13
        VehicleSpecification vehicleSpec = vehicleSpecificationRepository.findById(13L)
                .orElseThrow(() -> new RuntimeException("VehicleSpecification not found"));

        Ride ride = new Ride();
        ride.setRoute(route);
        ride.setPassengers(List.of((Passenger) passenger));
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setScheduledFor(request.getScheduledFor() != null
                ? request.getScheduledFor()
                : LocalDateTime.now());

        ride.setVehicleSpecification(vehicleSpec);

        ride.setPrice(calculatePrice(ride));

        return rideRepository.save(ride);
    }

    // ================= admin ========================
    public RideStatusResponse getRideStatus(Long rideId) {
        Ride ride = getRideOrThrow(rideId);
        return new RideStatusResponse(ride.getId(), ride.getStatus().name(), ride.getScheduledFor(), ride.getStartTime(), ride.getEndTime(), null, null, null, null, ride.getPrice());
    }

    public RideStatusResponse getCurrentRideByDriver(Long driverId) {
        Ride ride = rideRepository.findCurrentRideByDriver(driverId)
                .orElseThrow(() ->
                        new RideNotFoundException("no active ride for this driver")
                );

        return new RideStatusResponse(ride.getId(), ride.getStatus().name(), ride.getScheduledFor(), ride.getStartTime(), ride.getEndTime(), null, null, null, null, ride.getPrice());
    }

    // ================= location updates =================

    @Transactional
    public void updateVehicleLocation(Long rideId, Double latitude, Double longitude) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getDriver() == null || ride.getDriver().getVehicle() == null) {
            throw new IllegalStateException("Ride does not have an assigned vehicle");
        }

        Vehicle vehicle = ride.getDriver().getVehicle();
        Location location = vehicle.getLocation();

        // Create location if it doesn't exist
        if (location == null) {
            location = new Location();
            vehicle.setLocation(location);
        }

        // Update coordinates
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        // Save the ride (vehicle location is persisted via cascade)
        rideRepository.save(ride);
    }

    @Transactional
    public void updatePassengerLocation(Long rideId, Double latitude, Double longitude) {
        Ride ride = getRideOrThrow(rideId);

        System.out.println(String.format(
                "Passenger location updated for ride %d: lat=%.6f, lng=%.6f at %s",
                rideId,
                latitude,
                longitude,
                LocalDateTime.now()
        ));
    }

    // ================= ride actions =================

    @Transactional
    public void cancelRide(
            Long rideId,
            String personEmail,
            CancelRideRequest request
    ) {
        Ride ride = getRideOrThrow(rideId);

        if (isDriver(ride, personEmail)) {
            cancelByDriver(ride, request);
        } else if (isPassenger(ride, personEmail)) {
            cancelByPassenger(ride, request);
        } else {
            throw new AccessDeniedException("not authorized");
        }

        ride.setCancellationReason(request.reason());
        rideRepository.save(ride);
    }

    @Transactional
    public void panicRide(
            Long rideId,
            String personEmail,
            PanicRideRequest request
    ) {
        Ride ride = getRideOrThrow(rideId);

        boolean isParticipant =
                ride.getDriver().getEmail().equals(personEmail)
                        || ride.containsPassengerWithEmail(personEmail);

        if (!isParticipant) {
            throw new AccessDeniedException("not part of this ride");
        }

        Person person = personRepository.findByEmail(personEmail)
                .orElseThrow(() -> new PersonNotFoundException("person not found"));

        ride.setStatus(RideStatus.PANIC);
        ride.setEndTime(LocalDateTime.now());

        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setPerson(person);
        panic.setVehicleLocation(request.vehicleLocation());

        panicRepository.save(panic);
        rideRepository.save(ride);
    }

    @Transactional
    public StopRideResponse stopRide(
            Long rideId,
            Person driver,
            StopRideRequest request
    ) {
        Ride ride = getRideOrThrow(rideId);

        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new AccessDeniedException("not authorized");
        }

        Route updatedRoute = ride.getRoute(); // placeholder
        ride.stop(updatedRoute);
        ride.setPrice(calculatePrice(ride));

        rideRepository.save(ride);
        return new StopRideResponse(ride.getPrice());
    }

    @Transactional
    public void startRide(Person driver, Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(
            () -> {throw new RideNotFoundException("Can't find a ride to start.");}
        );

        if (ride.getDriver().getId() != driver.getId()) {
            throw new ForeignRideException("Only a driver of this ride can start it.");
        }

        if (ride.getStatus() == RideStatus.ONGOING || ride.getStartTime() != null)
            throw new RideAlreadyStartedException();

        ride.setStatus(RideStatus.ONGOING);
        LocalDateTime now = LocalDateTime.now();
        ride.setStartTime(now);
        ride.setEndTime(now.plusSeconds(ride.getRoute().getEstimatedTimeSeconds()));
        
        rideRepository.save(ride);
    }

    @Transactional
    public void rideOrder(OrderRequest request) {
        RidePreferences preferences = request.preferences();
        RideRoute routeData = request.route();

        Ride ride = new Ride();
        ride.setCreatedAt(request.createdAt());
        ride.setScheduledFor(preferences.scheduledFor());
        // TODO: Set start time after we find driver...
        // TODO: Set end time after we find driver...
        ride.setStatus(RideStatus.CREATED);

        Route route = routeService.createRoute(routeData.pickup(), routeData.destination(), routeData.stops());
        ride.setRoute(route);
        ride.setPrice(calculatePrice(
            preferences.vehicleType(), route.getDistanceKm()
        ));

        // Find passengers by email:
        ride.setPassengers(passengerService.getTrackingPassengers(request.trackers()));

        // Finding the driver:
        Driver driver = driverService.findById(9L);
        // Driver driver = driverService.findDriverForRide(routeData.pickup().getLocation(), preferences);
        ride.setDriver(driver);
        ride.setVehicleSpecification(driver.getVehicle().getSpecification());

        rideRepository.save(ride);
    }

    // ================= helpers =================

    private Ride getRideOrThrow(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));
    }

    private void validateDriverAccess(Ride ride, Long driverId) {
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("unauthorized");
        }
    }

    private void validatePassengerAccess(Ride ride, Long passengerId) {
        boolean isPassenger = ride.getPassengers().stream()
                .anyMatch(p -> p.getId().equals(passengerId));

        if (!isPassenger) {
            throw new AccessDeniedException("unauthorized");
        }
    }

    private boolean isDriver(Ride ride, String email) {
        return ride.getDriver().getEmail().equals(email);
    }

    private boolean isPassenger(Ride ride, String email) {
        return ride.containsPassengerWithEmail(email);
    }

    private void cancelByDriver(Ride ride, CancelRideRequest request) {
        if (request.reason().isBlank()) {
            throw new IllegalArgumentException("reason required");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_DRIVER);
    }

    private void cancelByPassenger(Ride ride, CancelRideRequest request) {
        if (LocalDateTime.now().isAfter(ride.getStartTime().minusMinutes(10))) {
            throw new CancelRideExpiredException("cancel expired");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_PASSENGER);
    }

    private Double calculatePrice(Ride ride) {
        VehicleType type = ride.getVehicleSpecification().getType();

        Double basePrice =
                priceListService.calculateBasePrice(type);

        Double kmPrice =
                priceListService.getKmPrice();

        return basePrice + ride.getRoute().getDistanceKm() * kmPrice;
    }

    private Double calculatePrice(VehicleType type, double kilometers) {
        return priceListService.calculateBasePrice(type) + kilometers * priceListService.getKmPrice();
    }

    private Integer round(Double value) {
        return value != null ? (int) Math.round(value) : null;
    }

}
