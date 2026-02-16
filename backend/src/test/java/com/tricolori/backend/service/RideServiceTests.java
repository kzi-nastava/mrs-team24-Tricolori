package com.tricolori.backend.service;

import com.tricolori.backend.dto.osrm.OSRMRouteResponse;
import com.tricolori.backend.dto.ride.StopRideRequest;
import com.tricolori.backend.dto.ride.StopRideResponse;
import com.tricolori.backend.entity.*;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.repository.TrackingTokenRepository;
import com.tricolori.backend.repository.VehicleRepository;
import com.tricolori.backend.util.TestObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
}