package com.tricolori.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tricolori.backend.dto.ride.RidePreferences;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.DriverDailyLog;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.entity.Vehicle;
import com.tricolori.backend.entity.VehicleSpecification;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.enums.VehicleType;
import com.tricolori.backend.exception.NoSuitableDriversException;
import com.tricolori.backend.repository.DriverRepository;
import com.tricolori.backend.repository.RideRepository;
import com.tricolori.backend.util.TestObjectFactory;

@ExtendWith(MockitoExtension.class)
class DriverServiceTests {

    @Mock
    private DriverRepository repository;

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private DriverService driverService;

    private final Location pickupLocation = TestObjectFactory.createTestLocation(19.8335, 45.2671);

    private Driver createActiveDriverWithVehicle(Long id, VehicleSpecification spec, Location vehicleLocation) {
        Driver driver = TestObjectFactory.createTestDriverWithId(id);
        Vehicle vehicle = TestObjectFactory.createTestVehicle(spec);
        vehicle.setLocation(vehicleLocation);
        driver.setVehicle(vehicle);

        DriverDailyLog log = TestObjectFactory.createTestDailyLog(LocalDate.now(), true, driver);
        log.setActiveTimeSeconds(1000L); // daleko od 8h
        driver.setDailyLogs(new ArrayList<>(List.of(log)));

        return driver;
    }

    private Driver createActiveDriverWithVehicle(Long id) {
        return createActiveDriverWithVehicle(id, TestObjectFactory.createTestVehicleSpecification(), pickupLocation);
    }

    private RidePreferences standardPrefs() {
        return new RidePreferences(VehicleType.STANDARD, false, false, null);
    }

    // -------------------------------------------------------------------------
    // Nema aktivnih vozaca
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw NoSuitableDriversException when no active drivers exist")
    void findDriverForRide_ShouldThrow_WhenNoActiveDrivers() {
        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of());

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }

    // -------------------------------------------------------------------------
    // Radno vrijeme
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw NoSuitableDriversException when all drivers exceeded 8h work time")
    void findDriverForRide_ShouldThrow_WhenAllDriversExceededWorkTime() {
        Driver driver = createActiveDriverWithVehicle(1L);
        driver.getDailyLogs().get(0).setActiveTimeSeconds(8L * 60 * 60);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }

    @Test
    @DisplayName("Should throw when driver has no daily log for today")
    void findDriverForRide_ShouldThrow_WhenDriverHasNoDailyLogForToday() {
        Driver driver = TestObjectFactory.createTestDriverWithId(1L);
        driver.setVehicle(TestObjectFactory.createTestVehicle());

        // Log postoji ali za juce
        DriverDailyLog yesterdayLog = TestObjectFactory.createTestDailyLog(LocalDate.now().minusDays(1), true, driver);
        yesterdayLog.setActiveTimeSeconds(0L);
        driver.setDailyLogs(new ArrayList<>(List.of(yesterdayLog)));

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }

    @Test
    @DisplayName("Should include driver who worked less than 8h today")
    void findDriverForRide_ShouldAccept_DriverUnder8HWorkTime() {
        Driver driver = createActiveDriverWithVehicle(1L);
        driver.getDailyLogs().get(0).setActiveTimeSeconds(7L * 60 * 60); // 7h

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(driver, result);
    }

    // -------------------------------------------------------------------------
    // Preferencije vozila
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw when no driver matches requested vehicle type")
    void findDriverForRide_ShouldThrow_WhenNoDriverMatchesVehicleType() {
        Driver driver = createActiveDriverWithVehicle(1L); // STANDARD

        RidePreferences luxuryPrefs = new RidePreferences(VehicleType.LUXURY, false, false, null);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, luxuryPrefs, 1)
        );
    }

    @Test
    @DisplayName("Should throw when driver vehicle is not pet friendly but pet friendly requested")
    void findDriverForRide_ShouldThrow_WhenPetFriendlyNotAvailable() {
        VehicleSpecification spec = VehicleSpecification.builder()
            .type(VehicleType.STANDARD)
            .numSeats(4)
            .petFriendly(false)
            .babyFriendly(false)
            .model("Pezo 307")
            .build();

        Driver driver = createActiveDriverWithVehicle(1L, spec, pickupLocation);

        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, true, false, null);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, prefs, 1)
        );
    }

    @Test
    @DisplayName("Should throw when driver vehicle is not baby friendly but baby friendly requested")
    void findDriverForRide_ShouldThrow_WhenBabyFriendlyNotAvailable() {
        VehicleSpecification spec = VehicleSpecification.builder()
            .type(VehicleType.STANDARD)
            .numSeats(4)
            .petFriendly(false)
            .babyFriendly(false)
            .model("Pezo 307")
            .build();

        Driver driver = createActiveDriverWithVehicle(1L, spec, pickupLocation);

        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, false, true, null);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, prefs, 1)
        );
    }

    @Test
    @DisplayName("Should throw when vehicle does not have enough seats for passenger count")
    void findDriverForRide_ShouldThrow_WhenNotEnoughSeats() {
        VehicleSpecification spec = VehicleSpecification.builder()
            .type(VehicleType.STANDARD)
            .numSeats(2)
            .petFriendly(false)
            .babyFriendly(false)
            .model("Pezo 307")
            .build();

        Driver driver = createActiveDriverWithVehicle(1L, spec, pickupLocation);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        // 3 putnika, auto ima 2 mjesta
        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 3)
        );
    }

    @Test
    @DisplayName("Should accept driver when vehicle has exactly enough seats")
    void findDriverForRide_ShouldAccept_WhenExactSeatCount() {
        VehicleSpecification spec = VehicleSpecification.builder()
            .type(VehicleType.STANDARD)
            .numSeats(3)
            .petFriendly(false)
            .babyFriendly(false)
            .model("Pezo 307")
            .build();

        Driver driver = createActiveDriverWithVehicle(1L, spec, pickupLocation);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 3);

        assertEquals(driver, result);
    }

    @Test
    @DisplayName("Should accept pet+baby friendly driver when both requested")
    void findDriverForRide_ShouldAccept_WhenBothPetAndBabyFriendly() {
        VehicleSpecification spec = VehicleSpecification.builder()
            .type(VehicleType.STANDARD)
            .numSeats(4)
            .petFriendly(true)
            .babyFriendly(true)
            .model("Pezo 307")
            .build();

        Driver driver = createActiveDriverWithVehicle(1L, spec, pickupLocation);

        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, true, true, null);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, prefs, 1);

        assertEquals(driver, result);
    }

    // -------------------------------------------------------------------------
    // Slobodni vozaci - pronalazak najblizeg
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return closest free driver when multiple free drivers exist")
    void findDriverForRide_ShouldReturnClosest_WhenMultipleFreeDrivers() {
        // Driver1 je blizu pickupa (19.8335, 45.2671)
        Driver driver1 = createActiveDriverWithVehicle(1L,
            TestObjectFactory.createTestVehicleSpecification(),
            new Location(19.8340, 45.2675));

        // Driver2 je daleko
        Driver driver2 = createActiveDriverWithVehicle(2L,
            TestObjectFactory.createTestVehicleSpecification(),
            new Location(19.9000, 45.3000));

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver1, driver2));
        // Niko nema aktivnih voznji danas -> oba su slobodna...
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(driver1, result);
    }

    @Test
    @DisplayName("Should return single free driver when only one exists")
    void findDriverForRide_ShouldReturnDriver_WhenOnlyOneFreeDriver() {
        Driver driver = createActiveDriverWithVehicle(1L);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(driver, result);
    }

    // -------------------------------------------------------------------------
    // Zakazana voznja - provjera zauzetosti u periodu
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw when all drivers are busy at scheduled time")
    void findDriverForRide_ShouldThrow_WhenAllDriversBusyAtScheduledTime() {
        Driver driver = createActiveDriverWithVehicle(1L);
        LocalDateTime scheduledFor = LocalDateTime.now().plusHours(2);

        // Voznja koja se preklapa sa scheduledFor
        Ride ongoingRide = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ongoingRide.setStartTime(scheduledFor.minusMinutes(10));
        ongoingRide.getRoute().setEstimatedTimeSeconds(3600L); // 1h - preklapa se

        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, false, false, scheduledFor);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));
        when(rideRepository.findAllByDriverAndStatusIn(eq(driver), any())).thenReturn(List.of(ongoingRide));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, prefs, 1)
        );
    }

    @Test
    @DisplayName("Should accept driver whose ride ends before scheduled time (outside overlap window)")
    void findDriverForRide_ShouldAccept_WhenDriverFreeAtScheduledTime() {
        Driver driver = createActiveDriverWithVehicle(1L);
        LocalDateTime scheduledFor = LocalDateTime.now().plusHours(3);

        // Voznja koja završava 1h prije scheduledFor + 5 min buffer = slobodan
        Ride ongoingRide = TestObjectFactory.createTestRide(driver, RideStatus.ONGOING);
        ongoingRide.setStartTime(LocalDateTime.now());
        ongoingRide.getRoute().setEstimatedTimeSeconds(600L); // 10 minuta - završava daleko prije

        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, false, false, scheduledFor);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));
        when(rideRepository.findAllByDriverAndStatusIn(eq(driver), any())).thenReturn(List.of(ongoingRide));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, prefs, 1);

        assertEquals(driver, result);
    }

    @Test
    @DisplayName("Should select free driver over busy driver for scheduled ride")
    void findDriverForRide_ShouldPreferFreeDriver_ForScheduledRide() {
        Driver freeDriver = createActiveDriverWithVehicle(1L);
        Driver busyDriver = createActiveDriverWithVehicle(2L);

        LocalDateTime scheduledFor = LocalDateTime.now().plusHours(2);

        Ride conflictingRide = TestObjectFactory.createTestRide(busyDriver, RideStatus.ONGOING);
        conflictingRide.setStartTime(scheduledFor.minusMinutes(10));
        conflictingRide.getRoute().setEstimatedTimeSeconds(3600L);

        RidePreferences prefs = new RidePreferences(VehicleType.STANDARD, false, false, scheduledFor);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(freeDriver, busyDriver));
        when(rideRepository.findAllByDriverAndStatusIn(eq(freeDriver), any())).thenReturn(List.of());
        when(rideRepository.findAllByDriverAndStatusIn(eq(busyDriver), any())).thenReturn(List.of(conflictingRide));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, prefs, 1);

        assertEquals(freeDriver, result);
    }

    // -------------------------------------------------------------------------
    // Zauzeti vozaci - fallback na best busy driver
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return busy driver whose ongoing ride ends within 10 minutes")
    void findDriverForRide_ShouldReturnBusyDriver_WhenRideEndsWithin10Minutes() {
        Driver busyDriver = createActiveDriverWithVehicle(1L);

        Ride ongoingRide = TestObjectFactory.createTestRide(busyDriver, RideStatus.ONGOING);
        ongoingRide.setStartTime(LocalDateTime.now().minusMinutes(55));
        ongoingRide.getRoute().setEstimatedTimeSeconds(3600L);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(busyDriver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(List.of(ongoingRide));
        when(rideRepository.findAllByDriverAndStatusIn(eq(busyDriver), any()))
            .thenReturn(List.of(ongoingRide));

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(busyDriver, result);
    }

    @Test
    @DisplayName("Should throw when busy driver's ongoing ride does not end within 10 minutes")
    void findDriverForRide_ShouldThrow_WhenBusyDriverRideNotEndingSoon() {
        Driver busyDriver = createActiveDriverWithVehicle(1L);

        // Ongoing voznja koja završava za 30 minuta
        Ride ongoingRide = TestObjectFactory.createTestRide(busyDriver, RideStatus.ONGOING);
        ongoingRide.setStartTime(LocalDateTime.now().minusMinutes(30));
        ongoingRide.getRoute().setEstimatedTimeSeconds(3600L); // završava za 30min

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(busyDriver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(List.of(ongoingRide));
        when(rideRepository.findAllByDriverAndStatusIn(eq(busyDriver), eq(List.of(RideStatus.ONGOING, RideStatus.SCHEDULED))))
            .thenReturn(List.of(ongoingRide));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }

    @Test
    @DisplayName("Should skip busy driver who has a SCHEDULED ride")
    void findDriverForRide_ShouldSkipBusyDriver_WhenHasScheduledRide() {
        Driver busyDriver = createActiveDriverWithVehicle(1L);

        Ride scheduledRide = TestObjectFactory.createTestRide(busyDriver, RideStatus.SCHEDULED);
        scheduledRide.setScheduledFor(LocalDateTime.now().plusHours(1));

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(busyDriver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(List.of(scheduledRide));
        when(rideRepository.findAllByDriverAndStatusIn(eq(busyDriver), eq(List.of(RideStatus.ONGOING, RideStatus.SCHEDULED))))
            .thenReturn(List.of(scheduledRide));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }

    @Test
    @DisplayName("Should return closest busy driver (by destination) when multiple ending soon")
    void findDriverForRide_ShouldReturnClosestBusyDriver_WhenMultipleEndingSoon() {
        // driver1 završava voznju blizu pickupa
        Driver driver1 = createActiveDriverWithVehicle(1L);
        Driver driver2 = createActiveDriverWithVehicle(2L);

        Ride ride1 = TestObjectFactory.createTestRide(driver1, RideStatus.ONGOING);
        ride1.setStartTime(LocalDateTime.now().minusMinutes(55));
        ride1.getRoute().setEstimatedTimeSeconds(3600L);
        // Destinacija blizu pickupa
        ride1.getRoute().getDestinationStop().setLocation(new Location(19.8340, 45.2675));

        Ride ride2 = TestObjectFactory.createTestRide(driver2, RideStatus.ONGOING);
        ride2.setStartTime(LocalDateTime.now().minusMinutes(55));
        ride2.getRoute().setEstimatedTimeSeconds(3600L);
        // Destinacija daleko od pickupa
        ride2.getRoute().getDestinationStop().setLocation(new Location(19.9500, 45.4000));

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver1, driver2));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(List.of(ride1, ride2));

        when(rideRepository.findAllByDriverAndStatusIn(eq(driver1), eq(List.of(RideStatus.ONGOING, RideStatus.SCHEDULED))))
            .thenReturn(List.of(ride1));
        when(rideRepository.findAllByDriverAndStatusIn(eq(driver1), eq(List.of(RideStatus.ONGOING))))
            .thenReturn(List.of(ride1));

        when(rideRepository.findAllByDriverAndStatusIn(eq(driver2), eq(List.of(RideStatus.ONGOING, RideStatus.SCHEDULED))))
            .thenReturn(List.of(ride2));
        when(rideRepository.findAllByDriverAndStatusIn(eq(driver2), eq(List.of(RideStatus.ONGOING))))
            .thenReturn(List.of(ride2));

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(driver1, result);
    }

    // -------------------------------------------------------------------------
    // Mijesani scenariji
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should prefer truly free driver over busy driver ending soon")
    void findDriverForRide_ShouldPreferFreeDriver_OverBusyDriverEndingSoon() {
        Driver freeDriver = createActiveDriverWithVehicle(1L,
            TestObjectFactory.createTestVehicleSpecification(),
            new Location(19.9500, 45.4000)); // daleko, ali slobodan

        Driver busyDriver = createActiveDriverWithVehicle(2L,
            TestObjectFactory.createTestVehicleSpecification(),
            new Location(19.8340, 45.2675)); // blizu, ali zauzet

        Ride ongoingRide = TestObjectFactory.createTestRide(busyDriver, RideStatus.ONGOING);
        ongoingRide.setStartTime(LocalDateTime.now().minusMinutes(55));
        ongoingRide.getRoute().setEstimatedTimeSeconds(3600L);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(freeDriver, busyDriver));
        // freeDriver nije u listi zauzetih danas
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(List.of(ongoingRide));

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(freeDriver, result);
    }

    @Test
    @DisplayName("Should throw when one driver matches preferences but exceeded work time")
    void findDriverForRide_ShouldThrow_WhenMatchingDriverExceededWorkTime() {
        // driver1 odgovara po vozilu ali je preradio
        Driver overworkedDriver = createActiveDriverWithVehicle(1L);
        overworkedDriver.getDailyLogs().get(0).setActiveTimeSeconds(8L * 60 * 60);

        // driver2 nije preradio ali ne odgovara po tipu vozila
        VehicleSpecification wrongSpec = VehicleSpecification.builder()
            .type(VehicleType.LUXURY)
            .numSeats(4)
            .petFriendly(false)
            .babyFriendly(false)
            .model("BMW 7")
            .build();
        Driver wrongTypeDriver = createActiveDriverWithVehicle(2L, wrongSpec, pickupLocation);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(overworkedDriver, wrongTypeDriver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }

    @Test
    @DisplayName("Should return correct driver when mix of eligible and ineligible drivers exist")
    void findDriverForRide_ShouldReturnEligible_WhenMixedDriverPool() {
        Driver eligibleDriver = createActiveDriverWithVehicle(1L);

        // Drugi vozac ima pogresno vozilo
        VehicleSpecification wrongSpec = VehicleSpecification.builder()
            .type(VehicleType.LUXURY)
            .numSeats(4)
            .petFriendly(false)
            .babyFriendly(false)
            .model("BMW 7")
            .build();
        Driver ineligibleDriver = createActiveDriverWithVehicle(2L, wrongSpec, pickupLocation);

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(eligibleDriver, ineligibleDriver));
        when(rideRepository.findAllByStatusInAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Driver result = driverService.findDriverForRide(pickupLocation, standardPrefs(), 1);

        assertEquals(eligibleDriver, result);
    }

    @Test
    @DisplayName("Should throw when driver has no daily logs at all")
    void findDriverForRide_ShouldThrow_WhenDriverHasNoDailyLogs() {
        Driver driver = TestObjectFactory.createTestDriverWithId(1L);
        driver.setVehicle(TestObjectFactory.createTestVehicle());
        driver.setDailyLogs(new ArrayList<>()); // prazna lista

        when(repository.getAllCurrentlyActiveDrivers()).thenReturn(List.of(driver));

        assertThrows(NoSuitableDriversException.class, () ->
            driverService.findDriverForRide(pickupLocation, standardPrefs(), 1)
        );
    }
}