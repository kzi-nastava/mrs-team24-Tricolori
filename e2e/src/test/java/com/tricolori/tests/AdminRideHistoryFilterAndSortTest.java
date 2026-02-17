package com.tricolori.tests;

import com.tricolori.pages.AdminNavbar;
import com.tricolori.pages.AdminRideHistoryPage;
import com.tricolori.pages.LoginPage;
import com.tricolori.pages.UnregisteredHomePage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AdminRideHistoryFilterAndSortTest {

    private WebDriver driver;
    private AdminRideHistoryPage historyPage;

    private static final String BASE_URL = "http://localhost:4200/";

    @BeforeEach
    public void setup() {

        driver = new ChromeDriver();

        driver.manage().window().maximize();

        driver.get(BASE_URL);

        historyPage = new AdminRideHistoryPage(driver);

        UnregisteredHomePage homePage = new UnregisteredHomePage(driver);
        homePage.clickLoginButton();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("admin@gmail.com", "Password123");
        AdminNavbar navbar = new AdminNavbar(driver);
        navbar.goToHistory();

        assertTrue(historyPage.isOpened(), "History page should be opened");
    }

    @AfterEach
    public void teardown() {

        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void shouldFilterRideHistoryByDateRange() {
        historyPage.waitForLoadingToFinish();

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);


        historyPage.filterByDate(
                "01", "01", "2026",
                "31", "12", "2026"
        );

        List<LocalDate> rideDates = historyPage.getRideDates();

        for (LocalDate rideDate : rideDates) {

            assertFalse(rideDate.isBefore(start), "Ride date " + rideDate + " is BEFORE start date");

            assertFalse(rideDate.isAfter(end), "Ride date " + rideDate + " is AFTER end date");
        }
    }

    @Test
    public void shouldClearFilters() {
        historyPage.waitForLoadingToFinish();

        historyPage.filterByEmail("passenger@test.com");

        int filteredCount = historyPage.getRideCount();

        historyPage.clearFilters();

        int clearedCount = historyPage.getRideCount();

        assertNotEquals(
                filteredCount,
                clearedCount,
                "Clearing filters should change results"
        );
    }

    @Test
    public void shouldSortRideHistoryByDateAscAndDesc() {
        historyPage.waitForLoadingToFinish();

        historyPage.clickSortByDate();
        List<LocalDate> ascDates = historyPage.getRideDates();

        assertFalse(ascDates.isEmpty(), "Ride list should not be empty");

        for (int i = 0; i < ascDates.size() - 1; i++) {
            LocalDate current = ascDates.get(i);
            LocalDate next = ascDates.get(i + 1);
            assertFalse(current.isAfter(next), "Dates are NOT sorted ascending: " + current + " > " + next);
        }

        historyPage.clickSortByDate();
        List<LocalDate> descDates = historyPage.getRideDates();

        assertFalse(descDates.isEmpty(), "Ride list should not be empty");

        for (int i = 0; i < descDates.size() - 1; i++) {
            LocalDate current = descDates.get(i);
            LocalDate next = descDates.get(i + 1);
            assertFalse(current.isBefore(next), "Dates are NOT sorted descending: " + current + " < " + next);
        }
    }

    @Test
    public void shouldSortByPriceAscAndDesc() {
        historyPage.waitForLoadingToFinish();

        historyPage.clickSortByPrice();
        historyPage.waitForLoadingToFinish();

        List<Double> ascPrices = historyPage.getRidePrices();
        assertFalse(ascPrices.isEmpty(), "Ride list should not be empty");

        for (int i = 0; i < ascPrices.size() - 1; i++) {
            double current = ascPrices.get(i);
            double next = ascPrices.get(i + 1);
            assertTrue(
                    current >= next,
                    "Prices are NOT sorted descending: " + current + " > " + next
            );
        }

        historyPage.clickSortByPrice();
        historyPage.waitForLoadingToFinish();

        List<Double> descPrices = historyPage.getRidePrices();
        assertFalse(descPrices.isEmpty(), "Ride list should not be empty");

        for (int i = 0; i < descPrices.size() - 1; i++) {
            double current = descPrices.get(i);
            double next = descPrices.get(i + 1);
            assertTrue(
                    current <= next,
                    "Prices are NOT sorted ascending: " + current + " < " + next
            );
        }
    }

    @Test
    public void shouldOpenRideDetailsModal() {
        historyPage.waitForLoadingToFinish();

        if (historyPage.getRideCount() == 0) {
            return;
        }

        historyPage.viewRideDetailsByIndex(0);

        assertTrue(
                historyPage.isRideDetailsModalOpened(),
                "Ride details modal should open"
        );
    }

    @Test
    public void shouldFilterRideHistoryByEmail() {
        historyPage.waitForLoadingToFinish();

        int initialCount = historyPage.getRideCount();

        historyPage.filterByEmail("passenger@test.com");

        int filteredCount = historyPage.getRideCount();

        assertTrue(
                filteredCount >= 0,
                "Filtered count should be valid"
        );

        assertNotEquals(
                initialCount,
                filteredCount,
                "Ride count should change after filtering"
        );
    }

}
