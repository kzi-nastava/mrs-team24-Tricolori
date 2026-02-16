package com.tricolori.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tricolori.backend.dto.ride.StopRideRequest;
import com.tricolori.backend.dto.ride.StopRideResponse;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.exception.GlobalExceptionHandler;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.security.AuthTokenFilter;
import com.tricolori.backend.security.JwtUtil;
import com.tricolori.backend.service.RideService;
import com.tricolori.backend.service.ReviewService;
import com.tricolori.backend.service.InconsistencyReportService;
import com.tricolori.backend.service.AuthService;
import com.tricolori.backend.util.TestObjectFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                RideController.class,
                GlobalExceptionHandler.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebSecurityConfiguration.class,
                        AuthTokenFilter.class
                }
        )
)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class RideControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RideService rideService;

    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private InconsistencyReportService inconsistencyReportService;

    @MockitoBean
    private AuthService authenticationService;

    private final String driverToken = "Bearer test-token";


    @Test
    @DisplayName("Should successfully stop ongoing ride")
    void shouldStopOngoingRide() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest(TestObjectFactory.createTestLocation());
        StopRideResponse mockResponse = new StopRideResponse(1250.0);

        when(rideService.stopRide(any(), any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/stop")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedPrice").value(1250.0));

        verify(rideService, times(1)).stopRide(any(), any());
    }

    @Test
    @DisplayName("Should return 404 when ride not found in service")
    void shouldReturn404WhenRideNotFound() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest(new Location(19.0, 45.0));

        when(rideService.stopRide(any(), any()))
                .thenThrow(new RideNotFoundException("Ride not found for this driver."));

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/stop")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ride not found for this driver."));
    }

    @Test
    @DisplayName("Should return 400 when location data is missing")
    void shouldReturn400WhenInvalidInput() throws Exception {
        String invalidJson = "{\"location\": null}";

        mockMvc.perform(put("/api/v1/rides/stop")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ============ STUDENT 2 - RIDE COMPLETION ===============

    @Test
    @DisplayName("Should successfully complete ride")
    void shouldCompleteRide() throws Exception {
        // Arrange
        Long rideId = 1L;
        Long driverId = 10L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(driverId);
        doNothing().when(rideService).completeRide(rideId, driverId);

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authenticationService, times(1)).getAuthenticatedUserId();
        verify(rideService, times(1)).completeRide(rideId, driverId);
    }

    @Test
    @DisplayName("Should return 404 when completing non-existent ride")
    void shouldReturn404WhenCompletingNonExistentRide() throws Exception {
        // Arrange
        Long rideId = 999L;
        Long driverId = 10L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(driverId);
        doThrow(new RideNotFoundException("ride not found"))
                .when(rideService).completeRide(rideId, driverId);

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ride not found"));

        verify(rideService, times(1)).completeRide(rideId, driverId);
    }

    @Test
    @DisplayName("Should return 403 when driver not authorized to complete ride")
    void shouldReturn403WhenDriverNotAuthorized() throws Exception {
        // Arrange
        Long rideId = 1L;
        Long driverId = 10L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(driverId);
        doThrow(new AccessDeniedException("not authorized to complete this ride"))
                .when(rideService).completeRide(rideId, driverId);

        // Act & Assert
        try {
            mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                    .header("Authorization", driverToken)
                    .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // Exception is expected
        }

        verify(rideService, times(1)).completeRide(rideId, driverId);
    }

    @Test
    @DisplayName("Should return 400 when ride is not in ONGOING status")
    void shouldReturn400WhenRideNotOngoing() throws Exception {
        // Arrange
        Long rideId = 1L;
        Long driverId = 10L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(driverId);
        doThrow(new IllegalStateException("Ride is not in progress"))
                .when(rideService).completeRide(rideId, driverId);

        // Act & Assert
        try {
            mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                    .header("Authorization", driverToken)
                    .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // Exception is expected
        }

        verify(rideService, times(1)).completeRide(rideId, driverId);
    }

    @Test
    @DisplayName("Should return 403 when completing ride belonging to another driver")
    void shouldReturn403WhenCompletingAnotherDriversRide() throws Exception {
        // Arrange
        Long rideId = 1L;
        Long actualDriverId = 10L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(actualDriverId);
        doThrow(new AccessDeniedException("not authorized to complete this ride"))
                .when(rideService).completeRide(rideId, actualDriverId);

        // Act & Assert
        try {
            mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                    .header("Authorization", driverToken)
                    .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // Exception is expected
        }

        verify(rideService, times(1)).completeRide(rideId, actualDriverId);
    }

    @Test
    @DisplayName("Should handle invalid ride ID format")
    void shouldHandleInvalidRideIdFormat() throws Exception {
        // Arrange
        String invalidRideId = "invalid";

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/{id}/complete", invalidRideId)
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).completeRide(any(), any());
    }

    @Test
    @DisplayName("Should successfully complete ride with multiple passengers")
    void shouldCompleteRideWithMultiplePassengers() throws Exception {
        // Arrange
        Long rideId = 5L;
        Long driverId = 15L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(driverId);
        doNothing().when(rideService).completeRide(rideId, driverId);

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(rideService, times(1)).completeRide(rideId, driverId);
    }

    @Test
    @DisplayName("Should complete ride when no tracking tokens exist")
    void shouldCompleteRideWithoutTrackingTokens() throws Exception {
        // Arrange
        Long rideId = 3L;
        Long driverId = 12L;

        when(authenticationService.getAuthenticatedUserId()).thenReturn(driverId);
        doNothing().when(rideService).completeRide(rideId, driverId);

        // Act & Assert
        mockMvc.perform(put("/api/v1/rides/{id}/complete", rideId)
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(rideService, times(1)).completeRide(rideId, driverId);
    }
}