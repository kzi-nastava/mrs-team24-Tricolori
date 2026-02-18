package com.tricolori.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.tricolori.backend.entity.Route;
import com.tricolori.backend.util.TestObjectFactory;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RouteRepositoryTests {
    private final RouteRepository routeRepository;

    @Autowired
    public RouteRepositoryTests(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Test
    public void FindByRouteGeometry_ShouldFind() {
        // Arrange:
        Route testRoute = TestObjectFactory.createTestRoute();
        String geometryString = testRoute.getRouteGeometry();

        routeRepository.save(testRoute);
        
        // Act
        Optional<Route> foundRoute = routeRepository.findByRouteGeometry(geometryString);
        
        // Assert
        assertTrue(foundRoute.isPresent(), "Route should be found");
        assertEquals(geometryString, foundRoute.get().getRouteGeometry());
    }

    @Test
    public void FindByRouteGeometry_ShouldReturnEmpty_WhenGeometryDoesNotExist() {
        // Arrange
        Route testRoute = TestObjectFactory.createTestRoute();
        String existingGeometry = testRoute.getRouteGeometry();
        String nonExistingGeometry = "Random_geom_" + System.nanoTime() + 1;
        
        routeRepository.save(testRoute);
        
        // Act
        Optional<Route> foundRoute = routeRepository.findByRouteGeometry(nonExistingGeometry);
        
        // Assert
        assertNotEquals(nonExistingGeometry, existingGeometry);
        assertTrue(foundRoute.isEmpty(), "Route should not be found for non-existing geometry");
    }

    @Test
    public void FindByRouteGeometry_ShouldReturnEmpty_WhenNoRoutesExist() {
        // Arrange
        String geometry = "some_geometry";
        
        // Act
        Optional<Route> foundRoute = routeRepository.findByRouteGeometry(geometry);
        
        // Assert
        assertTrue(foundRoute.isEmpty(), "Route should not be found when repository is empty");
    }

    @Test
    public void FindByRouteGeometry_ShouldReturnCorrectRoutes_WhenMultipleRoutesExist() {
        
        // Arrange
        String geometry1 = "encoded_polyline_route1";
        String geometry2 = "encoded_polyline_route2";
        String geometry3 = "encoded_polyline_route3";
        
        Route route1 = TestObjectFactory.createTestRoute();
        route1.setRouteGeometry(geometry1);
        Route route2 = TestObjectFactory.createTestRoute();
        route2.setRouteGeometry(geometry2);
        Route route3 = TestObjectFactory.createTestRoute();
        route3.setRouteGeometry(geometry3);
        
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        
        // Act
        Optional<Route> foundRoute1 = routeRepository.findByRouteGeometry(geometry1);
        Optional<Route> foundRoute2 = routeRepository.findByRouteGeometry(geometry2);
        Optional<Route> foundRoute3 = routeRepository.findByRouteGeometry(geometry3);
        
        // Assert
        assertTrue(foundRoute1.isPresent(), "Route 1 should be found");
        assertTrue(foundRoute2.isPresent(), "Route 2 should be found");
        assertTrue(foundRoute3.isPresent(), "Route 3 should be found");

        assertEquals(geometry1, foundRoute1.get().getRouteGeometry());
        assertEquals(geometry2, foundRoute2.get().getRouteGeometry());
        assertEquals(geometry3, foundRoute3.get().getRouteGeometry());
        
    }

    @Test
    public void FindByRouteGeometry_ShouldReturnEmpty_WhenGeometryIsNull() {
        // Arrange
        Route route = TestObjectFactory.createTestRoute();
        routeRepository.save(route);
        
        // Act
        Optional<Route> foundRoute = routeRepository.findByRouteGeometry(null);
        
        // Assert
        assertTrue(foundRoute.isEmpty(), "Route should not be found when searching with null geometry");
    }

    @Test
    public void FindByRouteGeometry_ShouldBeCaseSensitive() {
        
        // Arrange
        String geometryLowerCase = "encoded_polyline_lowercase";
        String geometryUpperCase = "ENCODED_POLYLINE_LOWERCASE";

        Route route = TestObjectFactory.createTestRoute();
        route.setRouteGeometry(geometryLowerCase);
        routeRepository.save(route);
        
        // Act
        Optional<Route> foundRoute = routeRepository.findByRouteGeometry(geometryUpperCase);
        
        // Assert
        assertTrue(foundRoute.isEmpty(), "Route should not be found - geometry search should be case sensitive");
    }
}
