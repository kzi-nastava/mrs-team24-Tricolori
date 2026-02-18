package com.tricolori.backend.service;

import com.tricolori.backend.dto.osrm.OSRMRouteResponse;
import com.tricolori.backend.dto.ride.OrderRequest;
import com.tricolori.backend.dto.ride.RidePreferences;
import com.tricolori.backend.dto.ride.RideRoute;
import com.tricolori.backend.dto.ride.StopRideRequest;
import com.tricolori.backend.dto.ride.StopRideResponse;
import com.tricolori.backend.entity.*;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import com.tricolori.backend.exception.NoFreeDriverCloseException;
import com.tricolori.backend.exception.NoSuitableDriversException;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.repository.TrackingTokenRepository;
import com.tricolori.backend.repository.VehicleRepository;
import com.tricolori.backend.util.TestObjectFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTests {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private OSRMService osrmService;
    @Mock
    private GeocodingService geocodingService;
    @Mock
    private PriceListService priceListService;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TrackingTokenRepository trackingTokenRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RouteService routeService;
    @Mock
    private PassengerService passengerService;
    @Mock
    private DriverService driverService;
    @Mock
    private TrackingTokenService trackingTokenService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RideService rideService;

    @Test
    void stopRide_ShouldThrowException_WhenNoOngoingRide() {

        // Arrange
        Driver driver = TestObjectFactory.createTestDriverWithId(1L);
        when(rideRepository.findOngoingRideByDriver(driver.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RideNotFoundException.class, () ->
                rideService.stopRide(driver, new StopRideRequest(TestObjectFactory.createTestLocation()))
        );
    }

    @Test
    void stopRide_ShouldSuccessfullyTerminateRide() {

        // Arrange
        Driver driver = TestObjectFactory.createTestDriverWithId(1L);
        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);

        Location stopLocation = TestObjectFactory.createTestLocation();
        StopRideRequest request = new StopRideRequest(stopLocation);

        when(rideRepository.findOngoingRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(geocodingService.getAddressFromCoordinates(anyDouble(), anyDouble()))
                .thenReturn("Nova Adresa");

        OSRMRouteResponse.OSRMRoute osrmRoute = new OSRMRouteResponse.OSRMRoute();
        osrmRoute.setDistance(2000.0);
        osrmRoute.setDuration(300L);
        osrmRoute.setGeometry("new_geometry");

        OSRMRouteResponse response = new OSRMRouteResponse();
        response.setRoutes(List.of(osrmRoute));

        when(osrmService.getRoute(anyList())).thenReturn(response);

        double mockBasePrice = 120.0;
        double mockKmPrice = 100.0;

        when(priceListService.calculateBasePrice(any(VehicleType.class))).thenReturn(mockBasePrice);
        when(priceListService.getKmPrice()).thenReturn(mockKmPrice);

        // Act
        StopRideResponse result = rideService.stopRide(driver, request);

        // Assert
        assertEquals(RideStatus.STOPPED, ride.getStatus());
        assertNotNull(ride.getEndTime());
        assertEquals("Nova Adresa", ride.getRoute().getDestinationStop().getAddress());
        assertEquals(2.0, ride.getRoute().getDistanceKm());

        verify(rideRepository, times(1)).save(ride);
        assertNotNull(result);
    }

    @Test
    void stopRide_ShouldUpdateAllRouteDetails() {

        // Arrange
        Driver driver = TestObjectFactory.createTestDriverWithId(1L);
        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        Location stopLocation = TestObjectFactory.createTestLocation();

        OSRMRouteResponse.OSRMRoute osrmRoute = new OSRMRouteResponse.OSRMRoute();
        osrmRoute.setDistance(3500.0);
        osrmRoute.setDuration(450L);
        osrmRoute.setGeometry("new_geometry_data");

        OSRMRouteResponse response = new OSRMRouteResponse();
        response.setRoutes(List.of(osrmRoute));

        when(rideRepository.findOngoingRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(osrmService.getRoute(any())).thenReturn(response);
        when(geocodingService.getAddressFromCoordinates(anyDouble(), anyDouble())).thenReturn("Finalna Adresa");
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);

        // Act
        rideService.stopRide(driver, new StopRideRequest(stopLocation));

        // Assert
        Route updatedRoute = ride.getRoute();
        assertEquals(3.5, updatedRoute.getDistanceKm());
        assertEquals(450L, updatedRoute.getEstimatedTimeSeconds());
        assertEquals("new_geometry_data", updatedRoute.getRouteGeometry());
        assertEquals("Finalna Adresa", updatedRoute.getDestinationStop().getAddress());

        // Price check: 100 + (3.5 * 50) = 100 + 175 = 275.0
        assertEquals(275.0, ride.getPrice());
        verify(rideRepository, times(1)).save(ride);
    }


    // ============ STUDENT 2 - RIDE COMPLETION ===============

    @Test
    void completeRide_ShouldThrowException_WhenRideNotFound() {
        // Arrange
        Long rideId = 999L;
        Long driverId = 1L;

        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RideNotFoundException.class, () ->
                rideService.completeRide(rideId, driverId)
        );

        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create tracking token for unregistered passengers")
    void rideOrder_ShouldCreateToken_ForUnregisteredPassenger() {
        // Arrange
        Person organizer = TestObjectFactory.createTestPassenger();
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"unregistered@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();
        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        
        // Samo organizator je u bazi, unregistered@test.com NIJE
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of((Passenger)organizer));
        
        Driver mockDriver = TestObjectFactory.createTestDriver();
        Vehicle mockVehicle = TestObjectFactory.createTestVehicle();
        mockDriver.setVehicle(mockVehicle);

        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        // Proveravamo da li je pozvan servis za token za neregistrovanog putnika
        verify(trackingTokenService, times(1)).createTrackingToken(eq("unregistered@test.com"), anyString(), any());
        // Proveravamo da li je poslata notifikacija neregistrovanom putniku
        verify(notificationService, times(1)).sendAddedToRideNotification(
            eq("unregistered@test.com"), any(), any(), any(), any(), any(), any()
        );
    }

    void completeRide_ShouldThrowException_WhenDriverNotAuthorized() {
        // Arrange
        Long rideId = 1L;
        Long actualDriverId = 5L;
        Long unauthorizedDriverId = 10L;

        Driver actualDriver = TestObjectFactory.createTestDriverWithId(actualDriverId);
        Ride ride = TestObjectFactory.createTestRide(actualDriver, RideStatus.ONGOING);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                rideService.completeRide(rideId, unauthorizedDriverId)
        );

        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void completeRide_ShouldThrowException_WhenRideNotOngoing() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.SCHEDULED);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                rideService.completeRide(rideId, driverId)
        );

        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void completeRide_ShouldSuccessfullyCompleteRideAndMarkVehicleAvailable() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        vehicle.setAvailable(false);
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        Passenger passenger = TestObjectFactory.createTestPassenger();
        ride.setPassengers(List.of(passenger));

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of());

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        assertEquals(RideStatus.FINISHED, ride.getStatus());
        assertNotNull(ride.getEndTime());
        assertTrue(vehicle.isAvailable());

        verify(rideRepository, times(1)).save(ride);
        verify(vehicleRepository, times(1)).save(vehicle);
        verify(notificationService, times(1)).sendRideCompletedNotification(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    void completeRide_ShouldCalculateFinalPrice() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ride.getRoute().setDistanceKm(5.0);
        ride.setPassengers(List.of());

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(150.0);
        when(priceListService.getKmPrice()).thenReturn(80.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of());

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        assertEquals(550.0, ride.getPrice());
        verify(rideRepository, times(1)).save(ride);
    }

    @Test
    void completeRide_ShouldNotifyAllRegisteredPassengers() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);

        Passenger passenger1 = TestObjectFactory.createTestPassenger();
        passenger1.setEmail("passenger1@test.com");
        passenger1.setFirstName("John");

        Passenger passenger2 = TestObjectFactory.createTestPassenger();
        passenger2.setEmail("passenger2@test.com");
        passenger2.setFirstName("Jane");

        ride.setPassengers(List.of(passenger1, passenger2));

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of());

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        verify(notificationService, times(2)).sendRideCompletedNotification(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    void completeRide_ShouldNotifyUnregisteredPassengersWithTrackingTokens() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ride.setPassengers(List.of());

        TrackingToken token1 = new TrackingToken();
        token1.setEmail("unregistered1@test.com");
        token1.setFirstName("Mike");

        TrackingToken token2 = new TrackingToken();
        token2.setEmail("unregistered2@test.com");
        token2.setFirstName(null);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of(token1, token2));

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        verify(notificationService, times(2)).sendRideCompletedNotification(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    void completeRide_ShouldHandleEmptyFirstNameInTrackingToken() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ride.setPassengers(List.of());

        TrackingToken token = new TrackingToken();
        token.setEmail("empty@test.com");
        token.setFirstName("");

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of(token));

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        verify(notificationService, times(1)).sendRideCompletedNotification(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    void completeRide_ShouldNotifyBothRegisteredAndUnregisteredPassengers() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);

        Passenger registeredPassenger = TestObjectFactory.createTestPassenger();
        registeredPassenger.setEmail("registered@test.com");
        registeredPassenger.setFirstName("Alice");
        ride.setPassengers(List.of(registeredPassenger));

        TrackingToken token = new TrackingToken();
        token.setEmail("unregistered@test.com");
        token.setFirstName("Bob");

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of(token));

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        verify(notificationService, times(2)).sendRideCompletedNotification(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    void completeRide_ShouldSetEndTimeToCurrentTime() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ride.setPassengers(List.of());

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of());

        LocalDateTime beforeCompletion = LocalDateTime.now();

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        assertNotNull(ride.getEndTime());
        assertTrue(ride.getEndTime().isAfter(beforeCompletion.minusSeconds(5)));
        assertTrue(ride.getEndTime().isBefore(LocalDateTime.now().plusSeconds(5)));
    }

    @Test
    void completeRide_ShouldThrowException_WhenRideAlreadyFinished() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.FINISHED);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                rideService.completeRide(rideId, driverId)
        );

        verify(rideRepository, never()).save(any());
    }

    @Test
    void completeRide_ShouldThrowException_WhenRideCancelled() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.CANCELLED_BY_DRIVER);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                rideService.completeRide(rideId, driverId)
        );

        verify(rideRepository, never()).save(any());
    }

    @Test
    void completeRide_ShouldWorkWithNoPassengersAtAll() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Driver driver = TestObjectFactory.createTestDriverWithId(driverId);
        Vehicle vehicle = TestObjectFactory.createTestVehicle();
        driver.setVehicle(vehicle);

        Ride ride = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ride.setPassengers(new ArrayList<>());

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(trackingTokenRepository.findByRideId(rideId)).thenReturn(List.of());

        // Act
        rideService.completeRide(rideId, driverId);

        // Assert
        assertEquals(RideStatus.FINISHED, ride.getStatus());
        verify(notificationService, never()).sendRideCompletedNotification(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        );
    }


    /*--- Student 1 - Ride Order */
    @Test
    @DisplayName("Should successfully order ride and return ride ID")
    void rideOrder_ShouldReturnRideId_WhenEverythingIsValid() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(42L);
            return r;
        });

        // Act
        Long result = rideService.rideOrder(organizer, request);

        // Assert
        assertEquals(42L, result);
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Should set ride status to SCHEDULED when driver is found")
    void rideOrder_ShouldSetStatusScheduled_WhenDriverFound() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(rideRepository).save(argThat(ride ->
            ride.getStatus() == RideStatus.SCHEDULED
        ));
    }

    @Test
    @DisplayName("Should assign driver and vehicle specification to ride")
    void rideOrder_ShouldAssignDriverAndVehicleSpec() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        Vehicle mockVehicle = TestObjectFactory.createTestVehicle();
        mockDriver.setVehicle(mockVehicle);

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(rideRepository).save(argThat(ride ->
            ride.getDriver().equals(mockDriver) &&
            ride.getVehicleSpecification().equals(mockVehicle.getSpecification())
        ));
    }

    @Test
    @DisplayName("Should calculate price based on vehicle type and route distance")
    void rideOrder_ShouldCalculatePrice_BasedOnVehicleTypeAndDistance() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute(); // distanceKm = 5.5
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle()); // VehicleType.STANDARD

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(VehicleType.STANDARD)).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert: 100 + (5.5 * 50) = 375.0
        verify(rideRepository).save(argThat(ride ->
            ride.getPrice() == 375.0
        ));
    }

    @Test
    @DisplayName("Should set route, createdAt and scheduledFor on ride")
    void rideOrder_ShouldSetRouteAndTimestamps() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 18, 10, 0);
        LocalDateTime scheduledFor = LocalDateTime.of(2026, 2, 18, 10, 15);

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);
        request.setCreatedAt(createdAt);
        // scheduledFor dolazi iz preferences
        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, true, true, scheduledFor);
        request.setPreferences(prefs);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(rideRepository).save(argThat(ride ->
            ride.getRoute().equals(mockRoute) &&
            ride.getCreatedAt().equals(createdAt) &&
            ride.getScheduledFor().equals(scheduledFor)
        ));
    }

    @Test
    @DisplayName("Should include organizer email when calling passengerService")
    void rideOrder_ShouldIncludeOrganizerEmail_WhenCallingPassengerService() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(passengerService).getTrackingPassengers(argThat(emails ->
            Arrays.asList(emails).contains("organizer@test.com")
        ));
    }

    @Test
    @DisplayName("Should pass combined email list (organizer + trackers) to passengerService")
    void rideOrder_ShouldPassCombinedEmailList_ToPassengerService() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"a@test.com", "b@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(passengerService).getTrackingPassengers(argThat(emails -> {
            List<String> list = Arrays.asList(emails);
            return list.contains("organizer@test.com") &&
                list.contains("a@test.com") &&
                list.contains("b@test.com") &&
                list.size() == 3;
        }));
    }

    @Test
    @DisplayName("Should call driverService with pickup location from route")
    void rideOrder_ShouldCallDriverService_WithPickupLocation() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Location expectedPickup = mockRoute.getPickupStop().getLocation();

        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(driverService).findDriverForRide(eq(expectedPickup), any(), anyInt());
    }

    @Test
    @DisplayName("Should call driverService with total passenger count including organizer")
    void rideOrder_ShouldPassTotalPassengerCount_ToDriverService() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"a@test.com", "b@test.com"}); // organizer + 2 = 3

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(driverService).findDriverForRide(any(), any(), eq(3));
    }

    @Test
    @DisplayName("Should notify registered tracker, skip organizer")
    void rideOrder_ShouldNotifyRegisteredTracker_SkipOrganizer() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        Passenger tracker = TestObjectFactory.createTestPassenger();
        tracker.setEmail("tracker@test.com");
        tracker.setFirstName("Tracker");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"tracker@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer, tracker));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(notificationService, never()).sendAddedToRideNotification(
            eq("organizer@test.com"), any(), any(), any(), any(), any(), any()
        );
        verify(notificationService, times(1)).sendAddedToRideNotification(
            eq("tracker@test.com"), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("Should not send tracker notifications when trackers list is null")
    void rideOrder_ShouldNotSendTrackerNotifications_WhenNoTrackers() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(notificationService, never()).sendAddedToRideNotification(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should create tracking token for unregistered tracker, not for registered ones")
    void rideOrder_ShouldCreateToken_OnlyForUnregisteredTrackers() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        Passenger registeredTracker = TestObjectFactory.createTestPassenger();
        registeredTracker.setEmail("registered@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"registered@test.com", "unregistered@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        // Samo organizer i registered su u bazi, unregistered NIJE
        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer, registeredTracker));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(trackingTokenService, times(1)).createTrackingToken(eq("unregistered@test.com"), anyString(), any());
        verify(trackingTokenService, never()).createTrackingToken(eq("registered@test.com"), anyString(), any());
        verify(trackingTokenService, never()).createTrackingToken(eq("organizer@test.com"), anyString(), any());
    }

    @Test
    @DisplayName("Should send notification to unregistered tracker")
    void rideOrder_ShouldSendNotification_ToUnregisteredTracker() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"unregistered@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        // Samo organizer je registrovan
        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(notificationService, times(1)).sendAddedToRideNotification(
            eq("unregistered@test.com"), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("Should not save ride when NoSuitableDriversException is thrown")
    void rideOrder_ShouldNotSaveRide_WhenNoSuitableDrivers() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        Route mockRoute = TestObjectFactory.createTestRoute();

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt()))
            .thenThrow(new NoSuitableDriversException("Nema vozaca."));

        // Act
        assertThrows(NoSuitableDriversException.class, () ->
            rideService.rideOrder(organizer, request)
        );

        // Assert
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should send rejection notifications to all emails when NoSuitableDriversException is thrown")
    void rideOrder_ShouldNotifyAll_WhenNoSuitableDrivers() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"tracker@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt()))
            .thenThrow(new NoSuitableDriversException("Nema vozaca."));

        // Act
        assertThrows(NoSuitableDriversException.class, () ->
            rideService.rideOrder(organizer, request)
        );

        // Assert - i organizer i tracker dobijaju rejection
        verify(notificationService).sendRideRejectedNotification(eq("organizer@test.com"), any());
        verify(notificationService).sendRideRejectedNotification(eq("tracker@test.com"), any());
        verify(notificationService, times(2)).sendRideRejectedNotification(any(), any());
    }

    @Test
    @DisplayName("Should send rejection notifications to all emails when NoFreeDriverCloseException is thrown")
    void rideOrder_ShouldNotifyAll_WhenNoFreeDriverClose() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"tracker@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt()))
            .thenThrow(new NoFreeDriverCloseException("Nema slobodnog vozaca u blizini."));

        // Act
        assertThrows(NoFreeDriverCloseException.class, () ->
            rideService.rideOrder(organizer, request)
        );

        // Assert
        verify(notificationService).sendRideRejectedNotification(eq("organizer@test.com"), any());
        verify(notificationService).sendRideRejectedNotification(eq("tracker@test.com"), any());
    }

    @Test
    @DisplayName("Should rethrow NoSuitableDriversException so global handler can catch it")
    void rideOrder_ShouldRethrow_NoSuitableDriversException() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        when(routeService.createRoute(any(), any(), any())).thenReturn(TestObjectFactory.createTestRoute());
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt()))
            .thenThrow(new NoSuitableDriversException("Nema vozaca."));

        // Act & Assert
        NoSuitableDriversException ex = assertThrows(NoSuitableDriversException.class, () ->
            rideService.rideOrder(organizer, request)
        );
        assertEquals("Nema vozaca.", ex.getMessage());
    }

    @Test
    @DisplayName("Should rethrow NoFreeDriverCloseException so global handler can catch it")
    void rideOrder_ShouldRethrow_NoFreeDriverCloseException() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        when(routeService.createRoute(any(), any(), any())).thenReturn(TestObjectFactory.createTestRoute());
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt()))
            .thenThrow(new NoFreeDriverCloseException("Nema slobodnog vozaca u blizini."));

        // Act & Assert
        NoFreeDriverCloseException ex = assertThrows(NoFreeDriverCloseException.class, () ->
            rideService.rideOrder(organizer, request)
        );
        assertEquals("Nema slobodnog vozaca u blizini.", ex.getMessage());
    }

    @Test
    @DisplayName("Should send rejection only to organizer when no trackers and driver not found")
    void rideOrder_ShouldSendRejectionOnlyToOrganizer_WhenNoTrackers() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        when(routeService.createRoute(any(), any(), any())).thenReturn(TestObjectFactory.createTestRoute());
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt()))
            .thenThrow(new NoSuitableDriversException("Nema vozaca."));

        // Act
        assertThrows(NoSuitableDriversException.class, () ->
            rideService.rideOrder(organizer, request)
        );

        // Assert
        verify(notificationService, times(1)).sendRideRejectedNotification(any(), any());
        verify(notificationService).sendRideRejectedNotification(eq("organizer@test.com"), any());
    }

    @Test
    @DisplayName("Should call routeService with pickup, destination and stops from request")
    void rideOrder_ShouldCallRouteService_WithCorrectRouteData() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(null);

        RideRoute rideRoute = request.getRoute();

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of(organizer));
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(routeService).createRoute(
            eq(rideRoute.pickup()),
            eq(rideRoute.destination()),
            eq(rideRoute.stops())
        );
    }

    @Test
    @DisplayName("Should set passengers list returned from passengerService on ride")
    void rideOrder_ShouldSetPassengerList_FromPassengerService() {
        // Arrange
        Passenger organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");

        Passenger tracker = TestObjectFactory.createTestPassenger();
        tracker.setEmail("tracker@test.com");

        OrderRequest request = TestObjectFactory.createOrderRequest();
        request.setTrackers(new String[]{"tracker@test.com"});

        Route mockRoute = TestObjectFactory.createTestRoute();
        Driver mockDriver = TestObjectFactory.createTestDriver();
        mockDriver.setVehicle(TestObjectFactory.createTestVehicle());

        List<Passenger> expectedPassengers = List.of(organizer, tracker);

        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        when(passengerService.getTrackingPassengers(any())).thenReturn(expectedPassengers);
        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        when(priceListService.calculateBasePrice(any())).thenReturn(100.0);
        when(priceListService.getKmPrice()).thenReturn(50.0);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(rideRepository).save(argThat(ride ->
            ride.getPassengers().equals(expectedPassengers)
        ));
    }
}