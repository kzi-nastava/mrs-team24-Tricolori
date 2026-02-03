package com.tricolori.backend.service;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.dto.ride.CancelRideRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestRideService {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideService rideService;

    private Ride mockRide;
    private Person driver;
    private Person passenger;

    @BeforeEach
    void setup() {
        mockRide = new Ride();
        mockRide.setId(1L);
        mockRide.setStartTime(LocalDateTime.now().plusHours(1));

        driver = new Person();
        driver.setId(10L);
        driver.setRole(PersonRole.ROLE_DRIVER);

        passenger = new Person();
        passenger.setId(20L);
        passenger.setRole(PersonRole.ROLE_PASSENGER);
    }

    @Test
    void testSuccessByDriver() {
        when(rideRepository.findCurrentRideByDriver(driver.getId()))
                .thenReturn(Optional.of(mockRide));

        CancelRideRequest req = new CancelRideRequest("Car breakdown");

        rideService.cancelRide(driver, req);

        verify(rideRepository).save(mockRide);
        assertNotNull(mockRide.getCancellationReason());
    }

    @Test
    void testSuccessByPassenger() {
        when(rideRepository.findCurrentRideByPassenger(passenger.getId()))
                .thenReturn(Optional.of(mockRide));

        CancelRideRequest req = new CancelRideRequest("Changed my mind");

        rideService.cancelRide(passenger, req);

        verify(rideRepository).save(mockRide);
    }

    @Test
    void testRideNotFoundForDriver() {
        when(rideRepository.findCurrentRideByDriver(driver.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> {
            rideService.cancelRide(driver, new CancelRideRequest("Any reason"));
        });

        verify(rideRepository, never()).save(any());
    }

    @Test
    void testAuthorizationDeniedForAdmin() {
        Person admin = new Person();
        admin.setRole(PersonRole.ROLE_ADMIN);

        assertThrows(AccessDeniedException.class, () -> {
            rideService.cancelRide(admin, new CancelRideRequest("Reason"));
        });
    }
}