package com.tricolori.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tricolori.backend.dto.ride.OrderRequest;
import com.tricolori.backend.dto.ride.StopRideRequest;
import com.tricolori.backend.dto.ride.StopRideResponse;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.exception.GlobalExceptionHandler;
import com.tricolori.backend.exception.RideNotFoundException;
import com.tricolori.backend.security.AuthTokenFilter;
import com.tricolori.backend.security.JwtUtil;
import com.tricolori.backend.service.RideService;
import com.tricolori.backend.service.ReviewService;
import com.tricolori.backend.service.InconsistencyReportService;
import com.tricolori.backend.service.AuthService;
import com.tricolori.backend.util.TestObjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

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

    
    /*--- Ride stoping: Student 3 ---*/

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

    /*--- Ride ordering: Student 1 ---*/
    @Test
    @DisplayName("Should successfully order a ride")
    void shouldOrderRideSuccessfully() throws Exception {
        // Arrange
        OrderRequest request = TestObjectFactory.createOrderRequest();
        
        doNothing().when(rideService).rideOrder(any(Person.class), any(OrderRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/rides/order")
                        .header("Authorization", "Bearer passenger-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Created a ride."));

        verify(rideService, times(1)).rideOrder(any(), any());
    }

    @Test
    @DisplayName("Should return error message when service throws exception")
    void shouldReturnErrorMessageOnServiceException() throws Exception {
        // Arrange
        OrderRequest request = TestObjectFactory.createOrderRequest();
        String errorMsg = "No drivers available";
        
        doThrow(new RuntimeException(errorMsg))
                .when(rideService).rideOrder(nullable(Person.class), any(OrderRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/rides/order")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ODGOVOR: RuntimeException: " + errorMsg)));

        verify(rideService, times(1)).rideOrder(nullable(Person.class), any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when request body is missing")
    void shouldReturn400WhenOrderRequestIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/rides/order")
                        .header("Authorization", "Bearer passenger-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}