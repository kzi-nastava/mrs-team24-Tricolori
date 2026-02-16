package com.tricolori.backend.repository;

import com.tricolori.backend.entity.*;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.util.TestObjectFactory; // Importuj tvoj factory
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


    // ========================================= STUDENT 2 =======================================

    @Test
    public void FindById_ShouldReturnRideWhenExists() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());

        // Assert
        assertTrue(foundRide.isPresent());
        assertEquals(savedRide.getId(), foundRide.get().getId());
        assertEquals(RideStatus.ONGOING, foundRide.get().getStatus());
    }

    @Test
    public void FindById_ShouldReturnEmpty_WhenRideDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;

        // Act
        Optional<Ride> foundRide = rideRepository.findById(nonExistentId);

        // Assert
        assertTrue(foundRide.isEmpty());
    }

    @Test
    public void Save_ShouldPersistRideStatusChange() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        savedRide.setStatus(RideStatus.FINISHED);
        savedRide.setEndTime(LocalDateTime.now());
        rideRepository.save(savedRide);

        // Assert
        Optional<Ride> updatedRide = rideRepository.findById(savedRide.getId());
        assertTrue(updatedRide.isPresent());
        assertEquals(RideStatus.FINISHED, updatedRide.get().getStatus());
        assertNotNull(updatedRide.get().getEndTime());
    }

    @Test
    public void Save_ShouldPersistPriceUpdate() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Double newPrice = 750.0;
        savedRide.setPrice(newPrice);
        rideRepository.save(savedRide);

        // Assert
        Optional<Ride> updatedRide = rideRepository.findById(savedRide.getId());
        assertTrue(updatedRide.isPresent());
        assertEquals(newPrice, updatedRide.get().getPrice());
    }

    @Test
    public void Save_ShouldPersistEndTime() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .startTime(LocalDateTime.now().minusMinutes(30))
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        LocalDateTime endTime = LocalDateTime.now();
        savedRide.setEndTime(endTime);
        savedRide.setStatus(RideStatus.FINISHED);
        rideRepository.save(savedRide);

        // Assert
        Optional<Ride> updatedRide = rideRepository.findById(savedRide.getId());
        assertTrue(updatedRide.isPresent());
        assertNotNull(updatedRide.get().getEndTime());
        assertTrue(updatedRide.get().getEndTime().isAfter(updatedRide.get().getStartTime()));
    }

    @Test
    public void FindById_ShouldLoadDriverRelationship() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());

        // Assert
        assertTrue(foundRide.isPresent());
        assertNotNull(foundRide.get().getDriver());
        assertEquals(driver.getId(), foundRide.get().getDriver().getId());
    }

    @Test
    public void FindById_ShouldLoadRouteRelationship() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());

        // Assert
        assertTrue(foundRide.isPresent());
        assertNotNull(foundRide.get().getRoute());
        assertEquals(route.getId(), foundRide.get().getRoute().getId());
    }

    @Test
    public void Save_ShouldUpdateMultipleFieldsSimultaneously() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .startTime(LocalDateTime.now().minusMinutes(15))
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        savedRide.setStatus(RideStatus.FINISHED);
        savedRide.setEndTime(LocalDateTime.now());
        savedRide.setPrice(850.0);
        Ride updatedRide = rideRepository.save(savedRide);

        // Assert
        Optional<Ride> foundRide = rideRepository.findById(updatedRide.getId());
        assertTrue(foundRide.isPresent());
        assertEquals(RideStatus.FINISHED, foundRide.get().getStatus());
        assertNotNull(foundRide.get().getEndTime());
        assertEquals(850.0, foundRide.get().getPrice());
    }

    @Test
    public void FindById_ShouldReturnRideWithFinishedStatus() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.FINISHED)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(600.0)
                .createdAt(LocalDateTime.now().minusHours(1))
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());

        // Assert
        assertTrue(foundRide.isPresent());
        assertEquals(RideStatus.FINISHED, foundRide.get().getStatus());
        assertNotNull(foundRide.get().getEndTime());
    }

    @Test
    public void FindById_ShouldReturnRideWithAllTimestamps() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        LocalDateTime created = LocalDateTime.now().minusHours(2);
        LocalDateTime started = LocalDateTime.now().minusHours(1);
        LocalDateTime ended = LocalDateTime.now();

        Ride ride = Ride.builder()
                .status(RideStatus.FINISHED)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(700.0)
                .createdAt(created)
                .startTime(started)
                .endTime(ended)
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());

        // Assert
        assertTrue(foundRide.isPresent());
        assertNotNull(foundRide.get().getCreatedAt());
        assertNotNull(foundRide.get().getStartTime());
        assertNotNull(foundRide.get().getEndTime());
        assertTrue(foundRide.get().getStartTime().isAfter(foundRide.get().getCreatedAt()));
        assertTrue(foundRide.get().getEndTime().isAfter(foundRide.get().getStartTime()));
    }

    @Test
    public void Save_ShouldAllowNullEndTime_WhenRideNotFinished() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .startTime(LocalDateTime.now())
                .endTime(null)
                .build();

        // Act
        Ride savedRide = rideRepository.save(ride);

        // Assert
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());
        assertTrue(foundRide.isPresent());
        assertNull(foundRide.get().getEndTime());
        assertEquals(RideStatus.ONGOING, foundRide.get().getStatus());
    }

    @Test
    public void FindById_ShouldReturnRideWithCorrectVehicleSpecification() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.FINISHED)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Act
        Optional<Ride> foundRide = rideRepository.findById(savedRide.getId());

        // Assert
        assertTrue(foundRide.isPresent());
        assertNotNull(foundRide.get().getVehicleSpecification());
        assertEquals(spec.getId(), foundRide.get().getVehicleSpecification().getId());
    }

    @Test
    public void Save_ShouldPreserveAllRideFieldsAfterCompletion() {
        // Arrange
        Driver driver = driverRepository.save(TestObjectFactory.createTestDriver());
        VehicleSpecification spec = vehicleSpecificationRepository.save(TestObjectFactory.createTestVehicleSpecification());
        Route route = routeRepository.save(TestObjectFactory.createTestRoute());

        Ride ride = Ride.builder()
                .status(RideStatus.ONGOING)
                .driver(driver)
                .vehicleSpecification(spec)
                .route(route)
                .price(500.0)
                .createdAt(LocalDateTime.now().minusHours(1))
                .startTime(LocalDateTime.now().minusMinutes(30))
                .build();

        Ride savedRide = rideRepository.save(ride);
        Long originalRideId = savedRide.getId();
        Double originalPrice = savedRide.getPrice();

        // Act
        savedRide.setStatus(RideStatus.FINISHED);
        savedRide.setEndTime(LocalDateTime.now());
        savedRide.setPrice(750.0);
        rideRepository.save(savedRide);

        // Assert
        Optional<Ride> completedRide = rideRepository.findById(originalRideId);
        assertTrue(completedRide.isPresent());
        assertEquals(originalRideId, completedRide.get().getId());
        assertEquals(RideStatus.FINISHED, completedRide.get().getStatus());
        assertEquals(750.0, completedRide.get().getPrice());
        assertNotEquals(originalPrice, completedRide.get().getPrice());
        assertNotNull(completedRide.get().getDriver());
        assertNotNull(completedRide.get().getRoute());
        assertNotNull(completedRide.get().getVehicleSpecification());
    }
}