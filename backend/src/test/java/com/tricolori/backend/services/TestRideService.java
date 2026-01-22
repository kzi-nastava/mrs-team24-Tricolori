package com.tricolori.backend.services;

import com.tricolori.backend.core.domain.models.Driver;
import com.tricolori.backend.core.domain.models.Passenger;
import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.repositories.RideRepository;
import com.tricolori.backend.core.exceptions.CancelRideExpiredException;
import com.tricolori.backend.core.services.RideService;
import com.tricolori.backend.infrastructure.presentation.dtos.CancelRideRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestRideService {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideService rideService;

    private Ride mockRide;

    @BeforeEach
    void setup() {
        mockRide = new Ride();
        mockRide.setId(1L);
        mockRide.setStartTime(LocalDateTime.now().plusHours(1));

        Driver driver = new Driver();
        driver.setEmail("driver@test.com");
        mockRide.setDriver(driver);

        Passenger passenger = new Passenger();
        passenger.setEmail("passenger@test.com");
        mockRide.setPassengers(List.of(passenger));
    }

    @Test
    void testCancellationExpired() {
        mockRide.setStartTime(LocalDateTime.now().plusMinutes(5));

        when(rideRepository.findById(1L)).thenReturn(Optional.of(mockRide));

        assertThrows(CancelRideExpiredException.class, () -> {
            rideService.cancelRide(1L, "passenger@test.com", new CancelRideRequest("Reason"));
        });

        verify(rideRepository, never()).save(any());
    }

    @Test
    void testCancellationReasonProvided() {
        mockRide.setCancellationReason("");

        when(rideRepository.findById(1L)).thenReturn(Optional.of(mockRide));

        assertThrows(IllegalArgumentException.class, () -> {
            rideService.cancelRide(1L, "driver@test.com", new CancelRideRequest(""));
        });

        verify(rideRepository, never()).save(any());
    }

    @Test
    void testAuthorizationDenied() {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(mockRide));

        assertThrows(AccessDeniedException.class, () -> {
            rideService.cancelRide(1L, "unknown@test.com", new CancelRideRequest("Reason"));
        });
    }

    @Test
    void testSuccess() {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(mockRide));
        CancelRideRequest req = new CancelRideRequest("Car breakdown");

        rideService.cancelRide(1L, "driver@test.com", req);

        verify(rideRepository).save(mockRide);
    }
}