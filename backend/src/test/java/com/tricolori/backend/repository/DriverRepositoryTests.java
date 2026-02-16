package com.tricolori.backend.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.util.TestObjectFactory;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class DriverRepositoryTests {
    private final DriverRepository driverRepository;

    @Autowired
    public DriverRepositoryTests(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldFindActiveDrivers() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driver.setDailyLogs(new ArrayList<>());
        driver.getDailyLogs().add(TestObjectFactory.createTestDailyLog(LocalDate.now(), true, driver));
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertEquals(1, activeDrivers.size(), "Should find one active driver");
        assertEquals(driver.getId(), activeDrivers.get(0).getId());
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldFindMultipleActiveDrivers() {
        // Arrange
        Driver driver1 = TestObjectFactory.createTestDriver();
        driver1.setDailyLogs(new ArrayList<>());
        driver1.getDailyLogs().add(TestObjectFactory.createTestDailyLog(LocalDate.now(), true, driver1));
        
        Driver driver2 = TestObjectFactory.createTestDriver();
        driver2.setDailyLogs(new ArrayList<>());
        driver2.getDailyLogs().add(TestObjectFactory.createTestDailyLog(LocalDate.now(), true, driver2));
        
        Driver driver3 = TestObjectFactory.createTestDriver();
        driver3.setDailyLogs(new ArrayList<>());
        driver3.getDailyLogs().add(TestObjectFactory.createTestDailyLog(LocalDate.now(), true, driver3));
        
        driverRepository.save(driver1);
        driverRepository.save(driver2);
        driverRepository.save(driver3);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertEquals(3, activeDrivers.size(), "Should find three active drivers");
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldReturnEmpty_WhenNoDriversAreActive() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driver.setDailyLogs(new ArrayList<>());
        driver.getDailyLogs().add(TestObjectFactory.createTestDailyLog(LocalDate.now(), false, driver));
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertTrue(activeDrivers.isEmpty(), "Should not find any active drivers when all are inactive");
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldReturnEmpty_WhenNoDriversExist() {
        // Arrange
        // No drivers in database
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertTrue(activeDrivers.isEmpty(), "Should not find any drivers when repository is empty");
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldNotIncludeDriversWithOldLogs() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driver.setDailyLogs(
            List.of(TestObjectFactory.createTestDailyLog(LocalDate.now().minusDays(1), true, driver)
        ));
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertTrue(activeDrivers.isEmpty(), "Should not find drivers with logs from previous days");
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldNotIncludeDriversWithFutureLogs() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driver.setDailyLogs(List.of(
            TestObjectFactory.createTestDailyLog(LocalDate.now().plusDays(1), true, driver)
        ));
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertTrue(activeDrivers.isEmpty(), "Should not find drivers with logs from future days");
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldFindOnlyActiveDrivers_WhenMixedActiveStatus() {
        // Arrange
        Driver activeDriver = TestObjectFactory.createTestDriver();
        activeDriver.setDailyLogs(List.of(
            TestObjectFactory.createTestDailyLog(LocalDate.now(), true, activeDriver)
        ));
        
        Driver inactiveDriver = TestObjectFactory.createTestDriver();
        inactiveDriver.setDailyLogs(List.of(
            TestObjectFactory.createTestDailyLog(LocalDate.now(), false, inactiveDriver)
        ));
        
        driverRepository.save(activeDriver);
        driverRepository.save(inactiveDriver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertEquals(1, activeDrivers.size(), "Should find only one active driver");
        assertEquals(activeDriver.getId(), activeDrivers.get(0).getId());
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldFindDriver_WhenHasMultipleLogs() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driver.setDailyLogs(List.of(
            TestObjectFactory.createTestDailyLog(LocalDate.now().minusDays(5), true, driver),
            TestObjectFactory.createTestDailyLog(LocalDate.now(), true, driver)
        ));
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertEquals(1, activeDrivers.size(), "Should find driver with today's active log");
        assertEquals(driver.getId(), activeDrivers.get(0).getId());
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldNotFindDriver_WhenTodayLogIsInactive() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driver.setDailyLogs(List.of(
            TestObjectFactory.createTestDailyLog(LocalDate.now().minusDays(1), true, driver),
            TestObjectFactory.createTestDailyLog(LocalDate.now(), false, driver)
        ));
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertTrue(activeDrivers.isEmpty(), "Should not find driver when today's log is inactive");
    }

    @Test
    public void GetAllCurrentlyActiveDrivers_ShouldReturnEmpty_WhenDriverHasNoLogs() {
        // Arrange
        Driver driver = TestObjectFactory.createTestDriver();
        driverRepository.save(driver);
        
        // Act
        List<Driver> activeDrivers = driverRepository.getAllCurrentlyActiveDrivers();
        
        // Assert
        assertTrue(activeDrivers.isEmpty(), "Should not find drivers without any logs");
    }
}