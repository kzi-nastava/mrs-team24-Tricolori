package com.tricolori.backend.service;

import com.tricolori.backend.dto.osrm.OSRMRouteResponse;
import com.tricolori.backend.dto.ride.OrderRequest;
import com.tricolori.backend.dto.ride.StopRideRequest;
import com.tricolori.backend.dto.ride.StopRideResponse;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.entity.Route;
import com.tricolori.backend.entity.Vehicle;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import com.tricolori.backend.exception.NoSuitableDriversException;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.util.TestObjectFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
    private RouteService routeService;
    @Mock
    private PassengerService passengerService;
    @Mock
    private DriverService driverService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TrackingTokenService trackingTokenService;

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


    /*--- Ride order - Student 1 ---*/
    @Test
    @DisplayName("Should successfully order a ride when driver is available")
    void rideOrder_ShouldSucceed_WhenDriverAvailable() {
        // Arrange
        Person organizer = TestObjectFactory.createTestPassenger();
        organizer.setEmail("organizer@test.com");
        OrderRequest request = TestObjectFactory.createOrderRequest();
        
        Route mockRoute = TestObjectFactory.createTestRoute();
        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        
        Passenger p2 = TestObjectFactory.createTestPassenger();
        p2.setEmail("friend@gmail.com"); // Email iz TestObjectFactory.createOrderRequest()
        when(passengerService.getTrackingPassengers(any())).thenReturn(List.of((Passenger)organizer, p2));
        
        Driver mockDriver = TestObjectFactory.createTestDriver();
        Vehicle mockVehicle = TestObjectFactory.createTestVehicle();
        mockDriver.setVehicle(mockVehicle);

        when(driverService.findDriverForRide(any(), any(), anyInt())).thenReturn(mockDriver);
        
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assertDoesNotThrow(() -> rideService.rideOrder(organizer, request));

        // Assert
        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(notificationService, times(1)).sendAddedToRideNotification(
            eq("friend@gmail.com"), any(), any(), any(), any(), any(), any()
        );
        verify(notificationService, never()).sendAddedToRideNotification(
            eq("organizer@test.com"), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("Should notify all passengers when no driver is found")
    void rideOrder_ShouldNotifyAll_WhenNoDriverFound() {
        // Arrange
        Person organizer = TestObjectFactory.createTestPassenger();
        OrderRequest request = TestObjectFactory.createOrderRequest(); // Sadrži organizatora i 1 tracker email
        
        Route mockRoute = TestObjectFactory.createTestRoute();
        when(routeService.createRoute(any(), any(), any())).thenReturn(mockRoute);
        
        // Simulišemo da nema vozača
        when(driverService.findDriverForRide(any(), any(), anyInt()))
                .thenThrow(new NoSuitableDriversException("No drivers"));

        // Act
        rideService.rideOrder(organizer, request);

        // Assert
        verify(notificationService, times(2)).sendRideRejectedNotification(anyString(), any());
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
}