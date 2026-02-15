package com.tricolori.backend.repository;

import com.tricolori.backend.entity.*;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.util.TestObjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RideRepositoryTests {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final VehicleSpecificationRepository vehicleSpecificationRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public RideRepositoryTests(
            RideRepository rideRepository,
            DriverRepository driverRepository,
            VehicleSpecificationRepository vehicleSpecificationRepository,
            RouteRepository routeRepository
    ) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.vehicleSpecificationRepository = vehicleSpecificationRepository;
        this.routeRepository = routeRepository;
    }

    @Test
    public void FindOngoingRideByDriver_ShouldFind() {

        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());

        VehicleSpecification vehicleSpecification = vehicleSpecificationRepository.save(
                TestObjectFactory.createTestVehicleSpecification()
        );

        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ongoingRide = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(vehicleSpecification)
                .route(route)
                .price(450.0)
                .createdAt(LocalDateTime.now())
                .build();

        rideRepository.save(ongoingRide);

        // Act
        Optional<Ride> foundRide = rideRepository.findOngoingRideByDriver(driver.getId());

        // Assert
        assertTrue(foundRide.isPresent(), "Ride should be found");
        assertEquals(RideStatus.ONGOING, foundRide.get().getStatus());
        assertEquals(driver.getId(), foundRide.get().getDriver().getId());
    }

    @Test
    public void FindOngoingRideByDriver_ShouldReturnEmpty_WhenRidesAreFinished() {

        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride finishedRide = Ride.builder()
                .status(RideStatus.FINISHED)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(450.0)
                .createdAt(LocalDateTime.now())
                .build();
        rideRepository.save(finishedRide);

        // Act
        Optional<Ride> foundRide = rideRepository.findOngoingRideByDriver(driver.getId());

        // Assert
        assertTrue(foundRide.isEmpty(), "Ride should not be found since its FINISHED");
    }

    @Test
    public void FindOngoingRideByDriver_ShouldReturnEmpty_WhenOngoingRideIsFromAnotherDriver() {

        // Arrange
        Driver driver1 = driverRepository.save(TestObjectFactory.createTestDriver());
        Driver driver2 = driverRepository.save(TestObjectFactory.createTestDriver());

        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ongoingRideDriver2 = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver2)
                .vehicleSpecification(spec)
                .route(route)
                .price(450.0)
                .createdAt(LocalDateTime.now())
                .build();
        rideRepository.save(ongoingRideDriver2);

        // Act
        Optional<Ride> foundRide = rideRepository.findOngoingRideByDriver(driver1.getId());

        // Assert
        assertTrue(foundRide.isEmpty(), "Ride should not be found since it belongs to different driver");
    }

    @Test
    public void FindOngoingRideByDriver_ShouldReturnEmpty_WhenDriverHasNoRidesAtAll() {

        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());

        // Act
        Optional<Ride> foundRide = rideRepository.findOngoingRideByDriver(driver.getId());

        // Assert
        assertTrue(foundRide.isEmpty(), "Ride should not be found since driver has no rides");
    }

    @Test
    public void FindOngoingRideByDriver_ShouldReturnOnlyOngoing_WhenDriverHasMultipleRides() {

        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route1 = routeRepository.save(TestObjectFactory.createTestRoute());
        Route route2 = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride finishedRide = Ride.builder()
                .status(RideStatus.FINISHED)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route1)
                .price(450.0)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        rideRepository.save(finishedRide);

        Ride ongoingRide = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route2)
                .price(600.0)
                .createdAt(LocalDateTime.now())
                .build();
        rideRepository.save(ongoingRide);

        // Act
        Optional<Ride> result = rideRepository.findOngoingRideByDriver(driver.getId());

        // Assert
        assertTrue(result.isPresent(), "Ride should be found");
        assertEquals(RideStatus.ONGOING, result.get().getStatus(), "Ride Status must be ONGOING");
        assertEquals(ongoingRide.getPrice(), result.get().getPrice(), "Exactly one ride should be returned");
    }
}