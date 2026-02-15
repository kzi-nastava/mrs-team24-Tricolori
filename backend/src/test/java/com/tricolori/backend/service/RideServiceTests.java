package com.tricolori.backend.service;

import com.tricolori.backend.dto.osrm.OSRMRouteResponse;
import com.tricolori.backend.dto.ride.StopRideRequest;
import com.tricolori.backend.dto.ride.StopRideResponse;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.entity.Route;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.util.TestObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}