package com.tricolori.backend.service;

import com.tricolori.backend.dto.history.AdminRideHistoryResponse;
import com.tricolori.backend.dto.osrm.OSRMRouteResponse;
import com.tricolori.backend.dto.profile.DriverDto;
import com.tricolori.backend.dto.profile.PassengerDto;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.exception.*;
import com.tricolori.backend.mapper.PersonMapper;
import com.tricolori.backend.mapper.RouteMapper;
import com.tricolori.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tricolori.backend.dto.ride.*;
import com.tricolori.backend.entity.*;
import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;
import com.tricolori.backend.mapper.RideMapper;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private final VehicleRepository vehicleRepository;
    private final PanicRepository panicRepository;
    private final OSRMService osrmService;
    private final GeocodingService geocodingService;

    private final PassengerService passengerService;
    private final DriverService driverService;
    private final NotificationService notificationService;
    private final TrackingTokenRepository trackingTokenRepository;
    private final TrackingTokenService trackingTokenService;

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
        response.setRouteId(ride.getRoute().getId());

        return response;
    }

    @Transactional
    public void completeRide(Long rideId, Long driverId) {
        Ride ride = getRideOrThrow(rideId);
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("not authorized to complete this ride");
        }
        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new IllegalStateException("Ride is not in progress");
        }

        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());
        // calculate final price
        ride.setPrice(calculatePrice(ride));
        rideRepository.save(ride);
        if (ride.getDriver() != null && ride.getDriver().getVehicle() != null) {
            ride.getDriver().getVehicle().setAvailable(true);
            vehicleRepository.save(ride.getDriver().getVehicle());
        }

        // notify registered passengers
        for (Passenger p : ride.getPassengers()) {
            notificationService.sendRideCompletedNotification(
                    p.getEmail(), p.getFirstName(), ride.getId(), ride.getRoute().getPickupStop().getAddress(),
                    ride.getRoute().getDestinationStop().getAddress(), ride.getPrice()
            );
        }

        // notify unregistered passengers who have tracking tokens
        List<TrackingToken> linkedPassengers = trackingTokenRepository.findByRideId(rideId);
        if (!linkedPassengers.isEmpty()) {
            log.info("Found {} linked passengers for completed ride {}", linkedPassengers.size(), rideId);

            for (TrackingToken token : linkedPassengers) {
                // Use "there" if firstName is null
                String firstName = token.getFirstName() != null && !token.getFirstName().isEmpty()
                        ? token.getFirstName()
                        : "there";

                notificationService.sendRideCompletedNotification(
                        token.getEmail(), firstName, ride.getId(), ride.getRoute().getPickupStop().getAddress(),
                        ride.getRoute().getDestinationStop().getAddress(), ride.getPrice()
                );
                log.info("Sent completion notification to linked passenger: {}", token.getEmail());
            }
        }
    }

    // ================= passenger =================

    public Page<PassengerRideHistoryResponse> getPassengerHistory(
            Person person, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        return rideRepository
                .findAllPassengerRides(person.getId(), start, end, pageable)
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
        response.setRouteId(ride.getRoute().getId());

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


    // ================= admin ========================

    public RideStatusResponse getCurrentRideByDriver(Long driverId) {
        Ride ride = rideRepository.findOngoingRideByDriver(driverId)
                .orElseThrow(() ->
                        new RideNotFoundException("no active ride for this driver")
                );

        return new RideStatusResponse(ride.getId(), ride.getStatus().name(), ride.getScheduledFor(), ride.getStartTime(), ride.getEndTime(), null, null, null, null, ride.getPrice());
    }

    @Transactional(readOnly = true)
    public List<RideTrackingResponse> getAllOngoingRides() {
        List<Ride> ongoingRides = rideRepository.findByStatus(RideStatus.ONGOING);
        return ongoingRides.stream()
                .map(this::toTrackingResponse)
                .toList();
    }

    // Single method to map Ride to RideTrackingResponse
    private RideTrackingResponse toTrackingResponse(Ride ride) {
        // Use mapper for basic fields
        RideTrackingResponse response = rideMapper.toTrackingResponse(ride);

        if (ride.getRoute() != null) {
            response = new RideTrackingResponse(
                    response.rideId(), response.status(), response.currentLocation(),
                    routeMapper.toDetailedRoute(ride.getRoute()), response.estimatedTimeMinutes(),
                    response.estimatedArrival(), response.scheduledFor(), response.startTime(),
                    response.price(), response.driver(), response.passengers()
            );
        }

        if (ride.getDriver() != null) {
            Double rating = null;
            try {
                rating = reviewService.getAverageDriverRating(ride.getDriver().getId());
            } catch (Exception ignored) {}

            DriverDto driverDto = personMapper.toDriverDto(ride.getDriver(), rating);

            response = new RideTrackingResponse(
                    response.rideId(), response.status(), response.currentLocation(), response.route(),
                    response.estimatedTimeMinutes(), response.estimatedArrival(), response.scheduledFor(),
                    response.startTime(), response.price(), driverDto, response.passengers()
            );
        }

        // TODO: add passenegers from ride tracking tokens table
        if (ride.getPassengers() != null && !ride.getPassengers().isEmpty()) {
            Long mainId = ride.getPassengers().getFirst().getId();

            List<PassengerDto> passengerDtos = ride.getPassengers().stream()
                    .map(p -> {
                        PassengerDto dto = personMapper.toPassengerDto(p);
                        dto.setMainPassenger(p.getId().equals(mainId));
                        return dto;
                    })
                    .toList();

            response = new RideTrackingResponse(
                    response.rideId(), response.status(), response.currentLocation(), response.route(),
                    response.estimatedTimeMinutes(), response.estimatedArrival(), response.scheduledFor(),
                    response.startTime(), response.price(), response.driver(), passengerDtos
            );
        }

        return response;
    }

    public Page<AdminRideHistoryResponse> getAdminRideHistory(
            String personEmail, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        return rideRepository
                .findAdminRideHistory(personEmail, start, end, pageable)
                .map(rideMapper::toAdminHistoryResponse);
    }

    public RideDetailResponse getAdminRideDetail(Long rideId) {
        Ride ride = getRideOrThrow(rideId);

        RideDetailResponse response = rideMapper.toDriverDetailResponse(ride);

        response.setDriverRating(
                round(reviewService.getAverageDriverRating(rideId))
        );
        response.setVehicleRating(
                round(reviewService.getAverageVehicleRating(rideId))
        );

        if (ride.getRoute() != null) {
            response.setRouteId(ride.getRoute().getId());
            response.setDistance(ride.getRoute().getDistanceKm());
        }

        response.setTotalPrice(ride.getPrice());

        return response;
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
    public void cancelRide(Person person, CancelRideRequest request) {

        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }

        String reason = (request != null && request.reason() != null && !request.reason().isBlank())
                ? request.reason()
                : "No reason provided";

        Ride ride;

        if (person.getRole() == PersonRole.ROLE_DRIVER) {
            ride = rideRepository.findOngoingRideByDriver(person.getId())
                    .orElseThrow(() -> new RideNotFoundException("Ride not found for this driver."));
        } else if (person.getRole() == PersonRole.ROLE_PASSENGER) {
            ride = rideRepository.findOngoingRideByPassenger(person.getId())
                    .orElseThrow(() -> new RideNotFoundException("Ride not found for this passenger."));
        } else {
            throw new IllegalStateException("Unsupported role: " + person.getRole());
        }

        String pickup = "Unknown pickup";
        String destination = "Unknown destination";

        if (ride.getRoute() != null) {
            if (ride.getRoute().getPickupStop() != null && ride.getRoute().getPickupStop().getAddress() != null) {
                pickup = ride.getRoute().getPickupStop().getAddress();
            }

            if (ride.getRoute().getDestinationStop() != null && ride.getRoute().getDestinationStop().getAddress() != null) {
                destination = ride.getRoute().getDestinationStop().getAddress();
            }
        }

        String scheduled = ride.getScheduledFor() != null
                ? ride.getScheduledFor().toString()
                : "N/A";

        if (person.getRole() == PersonRole.ROLE_DRIVER) { // DRIVER

            if (request == null || request.reason() == null || request.reason().isBlank()) {
                throw new IllegalArgumentException("Driver must provide cancellation reason");
            }

            cancelByDriver(ride, request);

            if (ride.getPassengers() != null) {
                for (Passenger p : ride.getPassengers()) {
                    if (p == null) continue;

                    notificationService.sendRideCancelledNotification(
                            p.getEmail(), ride.getId(), scheduled, pickup, destination, "Driver cancelled: " + reason
                    );
                }
            }

        } else { // PASSENGER

            if (ride.getStartTime() != null &&
                    LocalDateTime.now().isAfter(ride.getStartTime().minusMinutes(10))) {
                throw new CancelRideExpiredException("Cancel failed. Ride starts within 10 minutes");
            }

            cancelByPassenger(ride);

            if (ride.getDriver() != null) {
                notificationService.sendRideCancelledNotification(
                        ride.getDriver().getEmail(), ride.getId(), scheduled, pickup, destination, "Passenger cancelled the ride"
                );
            }
        }

        ride.setCancellationReason(reason);
        rideRepository.save(ride);

        log.info("Ride with id {} got cancelled by {}", ride.getId(), person.getEmail());
    }


    private void terminateRideAtLocation(Ride ride, Location stopLocation, RideStatus status) {

        Route route = ride.getRoute();
        Location pickup = route.getPickupStop().getLocation();

        // Find address display name from given coordinates
        String stopAddress = geocodingService.getAddressFromCoordinates(
                stopLocation.getLatitude(),
                stopLocation.getLongitude()
        );

        // Get updated route data
        OSRMRouteResponse osrmResponse = osrmService.getRoute(List.of(pickup, stopLocation));
        OSRMRouteResponse.OSRMRoute updatedRouteData = osrmResponse.getRoutes().getFirst();

        // Update route
        route.setDestinationStop(new Stop(stopAddress, stopLocation));
        route.setRouteGeometry(updatedRouteData.getGeometry());
        route.setEstimatedTimeSeconds(updatedRouteData.getDuration());

        double distanceKm = updatedRouteData.getDistance() / 1000.0;
        route.setDistanceKm(distanceKm);

        ride.setStatus(status);
        ride.setEndTime(LocalDateTime.now());

        ride.setPrice(calculatePrice(ride));
    }

    @Transactional
    public StopRideResponse stopRide(Person driver, StopRideRequest request) {
        Ride ride = rideRepository.findOngoingRideByDriver(driver.getId())
                .orElseThrow(() -> new RideNotFoundException("Ride not found for this driver."));

        terminateRideAtLocation(ride, request.location(), RideStatus.STOPPED);

        rideRepository.save(ride);
        return new StopRideResponse(ride.getPrice());
    }

    @Transactional
    public void panicRide(Person person, PanicRideRequest request) {

        Ride ride = person.getRole().equals(PersonRole.ROLE_PASSENGER) ?
                rideRepository.findOngoingRideByPassenger(person.getId())
                        .orElseThrow(() -> new RideNotFoundException("Ongoing ride not found for this passenger.")) :
                rideRepository.findOngoingRideByDriver(person.getId())
                        .orElseThrow(() -> new RideNotFoundException("Ongoing ride not found for this driver."));

        terminateRideAtLocation(ride, request.vehicleLocation(), RideStatus.PANIC);

        notificationService.sendPanicNotification(ride.getId(), person.getEmail());

        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setPerson(person);
        panic.setVehicleLocation(request.vehicleLocation());

        panicRepository.save(panic);
        rideRepository.save(ride);
    }

    @Transactional
    public void startRide(Person driver, Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(
            () -> {throw new RideNotFoundException("Can't find a ride to start.");}
        );

        if (!Objects.equals(ride.getDriver().getId(), driver.getId())) {
            throw new ForeignRideException("Only a driver of this ride can start it.");
        }

        if (ride.getStatus() == RideStatus.ONGOING)
            throw new RideAlreadyStartedException();

        ride.setStatus(RideStatus.ONGOING);
        LocalDateTime now = LocalDateTime.now();
        ride.setStartTime(now);
        ride.setEndTime(now.plusSeconds(ride.getRoute().getEstimatedTimeSeconds()));

        for (Passenger p : ride.getPassengers()) {
            notificationService.sendRideStartingNotification(
                    p.getEmail(), ride.getId(), ride.getDriver().getFirstName()+" "+ride.getDriver().getLastName(),
                    ride.getDriver().getVehicle().getModel(), ride.getRoute().getPickupStop().getAddress()
            );
        }
        notificationService.sendRideStartedNotification(ride.getDriver().getEmail(), ride.getId());
        
        rideRepository.save(ride);
        ride.getDriver().getVehicle().setAvailable(false); // mark vehicle as unavailable
        vehicleRepository.save(ride.getDriver().getVehicle());
    }

    @Transactional
    public void rideOrder(Person passenger, OrderRequest request) {
        RidePreferences preferences = request.getPreferences();

        // Extracting and creating route:
        RideRoute routeData = request.getRoute();
        Route route = routeService.createRoute(routeData.pickup(), routeData.destination(), routeData.stops());

        Ride ride = new Ride();
        ride.setCreatedAt(request.getCreatedAt());
        ride.setScheduledFor(preferences.scheduledFor());
        ride.setRoute(route);
        ride.setPrice(calculatePrice(
                preferences.vehicleType(), route.getDistanceKm()
        ));

        // Find passengers by email:
        // Add owner passenger as first email:
        List<String> allPassengerEmails = new ArrayList<>();
        allPassengerEmails.add(passenger.getEmail());
        if (request.getTrackers() != null) {
            allPassengerEmails.addAll(Arrays.asList(request.getTrackers()));
        }

        // Get only registered passengers for the ride's passenger list
        List<Passenger> trackingPassengers = passengerService.getTrackingPassengers(
                allPassengerEmails.toArray(new String[0])
        );
        ride.setPassengers(trackingPassengers);

        try {
            Driver driver = driverService.findDriverForRide(
                    route.getPickupStop().getLocation(),
                    preferences,
                    allPassengerEmails.size()
            );
            ride.setDriver(driver);
            ride.setStatus(RideStatus.SCHEDULED);
            ride.setVehicleSpecification(driver.getVehicle().getSpecification());

            ride = rideRepository.save(ride);

            String organizerName = passenger.getFirstName() + " " + passenger.getLastName();
            // separate registered and unregistered passengers
            List<String> registeredEmails = trackingPassengers.stream()
                    .map(Passenger::getEmail)
                    .toList();

            // Send notifications to registered passengers (skip organizer)
            for (Passenger p : trackingPassengers) {
                if (p.getEmail().equals(passenger.getEmail()))
                    continue; // skip main passenger (organizer)

                notificationService.sendAddedToRideNotification(
                        p.getEmail(), ride.getId(), organizerName, p.getFirstName(), ride.getRoute().getPickupStop().getAddress(),
                        ride.getRoute().getDestinationStop().getAddress(), ride.getScheduledFor().toString()
                );
            }

            // handle unregistered passengers (those in allPassengerEmails but not in trackingPassengers)
            for (String email : allPassengerEmails) {
                if (email.equals(passenger.getEmail())) {
                    continue; // skip organizer
                }

                if (!registeredEmails.contains(email)) {
                    // an unregistered passenger - create tracking token and send notification
                    log.info("Creating tracking token for unregistered passenger: {}", email);
                    String firstName = "there"; // so the email says hi there
                    String token = trackingTokenService.createTrackingToken(email, firstName, ride);
                    // Send notification
                    notificationService.sendAddedToRideNotification(
                            email, ride.getId(), organizerName, firstName, ride.getRoute().getPickupStop().getAddress(),
                            ride.getRoute().getDestinationStop().getAddress(), ride.getScheduledFor().toString()
                    );
                }
            }

        } catch (NoSuitableDriversException | NoFreeDriverCloseException e) {
            // Notify ALL passengers (registered and unregistered) about rejection
            for (String email : allPassengerEmails) {
                notificationService.sendRideRejectedNotification(email, null);
                log.info("Sent ride rejection notification to {}", email);
                log.info("Reason: {}", e.getMessage());
            }
        }
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

    private void cancelByDriver(Ride ride, CancelRideRequest request) {
        if (request.reason().isBlank()) {
            throw new IllegalArgumentException("reason required");
        }
        ride.setStatus(RideStatus.CANCELLED_BY_DRIVER);
    }

    private void cancelByPassenger(Ride ride) {
        if (LocalDateTime.now().isAfter(ride.getStartTime().minusMinutes(10))) {
            throw new CancelRideExpiredException("Cancel failed. Ride starts within 10 minutes");
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