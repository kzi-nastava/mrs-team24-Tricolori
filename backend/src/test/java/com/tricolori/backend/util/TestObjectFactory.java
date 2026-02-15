package com.tricolori.backend.util;

import com.tricolori.backend.entity.*;
import com.tricolori.backend.enums.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestObjectFactory {

    private static final Random random = new Random();

    // --- PASSENGER ---
    public static Passenger createTestPassenger() {
        return Passenger.builder()
                .firstName("Marko")
                .lastName("Dzek")
                .email("passenger." + System.nanoTime() + "@tricolori.com")
                .password("Password123")
                .phoneNum("+381612587282")
                .homeAddress("Bulevar Oslobodjenja 2, Novi Sad")
                .accountStatus(AccountStatus.ACTIVE)
                .role(PersonRole.ROLE_PASSENGER)
                .build();
    }

    // --- RIDE ---
    public static Ride createTestRide() {
        return createTestRide(createTestDriverWithId(random.nextLong(1000) + 1), RideStatus.ONGOING);
    }

    public static Ride createTestRide(RideStatus status) {
        return createTestRide(createTestDriverWithId(random.nextLong(1000) + 1), status);
    }

    public static Ride createTestRide(Driver driver, RideStatus status) {
        return Ride.builder()
                .id(random.nextLong(10000) + 1)
                .status(status)
                .driver(driver)
                .route(createTestRoute())
                .vehicleSpecification(createTestVehicleSpecification())
                .price(500.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- DRIVER ---
    public static Driver createTestDriver() {
        return Driver.builder()
                .firstName("Mali")
                .lastName("Bobi")
                .email("driver." + System.nanoTime() + "@tricolori.com")
                .password("Password123")
                .phoneNum("+381612587281")
                .homeAddress("Bulevar Oslobodjenja 1, Novi Sad")
                .accountStatus(AccountStatus.ACTIVE)
                .role(PersonRole.ROLE_DRIVER)
                .build();
    }

    public static Driver createTestDriverWithId(Long id) {
        Driver driver = createTestDriver();
        driver.setId(id);
        return driver;
    }

    public static VehicleSpecification createTestVehicleSpecification() {
        return VehicleSpecification.builder()
                .model("Pezo 307")
                .type(VehicleType.STANDARD)
                .numSeats(4)
                .babyFriendly(true)
                .petFriendly(true)
                .build();
    }

    // --- ROUTE, STOP, LOCATION ---
    public static Route createTestRoute() {
        List<Stop> stops = new ArrayList<>();
        stops.add(createTestStop("Glavna Stanica", 19.8335, 45.2671));
        stops.add(createTestStop("Centar", 19.8451, 45.2551));

        return Route.builder()
                .stops(stops)
                .distanceKm(5.5)
                .estimatedTimeSeconds(600L)
                .routeGeometry("random_geom_" + System.nanoTime())
                .build();
    }

    public static Stop createTestStop() {
        return createTestStop("Bulevar Oslobodjenja 1", 19.8335, 45.2671);
    }

    public static Stop createTestStop(String address, double lon, double lat) {
        return new Stop(address, new Location(lon, lat));
    }

    public static Location createTestLocation() {
        return new Location(19.8335, 45.2671);
    }

    public static Location createTestLocation(double lon, double lat) {
        return new Location(lon, lat);
    }
}