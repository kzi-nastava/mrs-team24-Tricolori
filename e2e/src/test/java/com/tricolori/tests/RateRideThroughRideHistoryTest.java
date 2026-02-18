package com.tricolori.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tricolori.pages.LoginPage;
import com.tricolori.pages.PassengerNavigation;
import com.tricolori.pages.RideDetailsModal;
import com.tricolori.pages.RideHistoryPassengerPage;
import com.tricolori.pages.RideRatingPage;
import com.tricolori.pages.UnregisteredHomePage;

public class RateRideThroughRideHistoryTest {
    private WebDriver driver;

    private UnregisteredHomePage unregisteredHomePage;
    private LoginPage loginPage;
    private PassengerNavigation navigationBar;
    private RideHistoryPassengerPage rideHistoryPage;
    private RideDetailsModal rideDetailsModal;
    private RideRatingPage rideRatingPage;

    private final static String PATH = "http://localhost:4200/";

    @BeforeMethod
    public void initialize() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--incognito", "--lang=sr-RS");

        this.driver = new ChromeDriver(options);
        this.driver.get(PATH);

        this.unregisteredHomePage = new UnregisteredHomePage(this.driver);
        this.loginPage = new LoginPage(this.driver);
        this.navigationBar = new PassengerNavigation(this.driver);
        this.rideHistoryPage = new RideHistoryPassengerPage(this.driver);
        this.rideDetailsModal = new RideDetailsModal(this.driver);
        this.rideRatingPage = new RideRatingPage(this.driver);
    }

    @AfterMethod
    public void uninitialize() {
        if (this.driver != null) {
            this.driver.quit();
        }
    }

    @DataProvider
    Object[][] ratingData() {
        return new Object[][] {
                {
                        "passenger.test@gmail.com",
                        "Password123",
                        "15", "02", "2026",
                        "20", "02", "2026",
                        0,
                        5,
                        4,
                        "Great ride! The driver was very professional and the vehicle was clean."
                }
        };
    }

    @DataProvider
    Object[][] minimumRatingData() {
        return new Object[][] {
                {
                        "passenger.test@gmail.com",
                        "Password123",
                        1,
                        1,
                        1,
                        null
                }
        };
    }

    @DataProvider
    Object[][] expiredRatingData() {
        return new Object[][] {
                {
                        "passenger.test@gmail.com",
                        "Password123",
                        "01", "02", "2026",
                        "10", "02", "2026",
                        0
                }
        };
    }

    @Test(dataProvider = "ratingData")
    public void testRateRideWithFullData(
            String email, String password,
            String startDay, String startMonth, String startYear,
            String endDay, String endMonth, String endYear,
            int rideIndex,
            int driverRating,
            int vehicleRating,
            String comment
    ) {
        assertTrue(unregisteredHomePage.isOpened());
        unregisteredHomePage.clickLoginButton();

        assertTrue(loginPage.isOpened());
        loginPage.login(email, password);

        navigationBar.openRideHistory();

        assertTrue(rideHistoryPage.isOpened());
        rideHistoryPage.filterRides(startDay, startMonth, startYear, endDay, endMonth, endYear);
        rideHistoryPage.viewDetailsByRideIndex(rideIndex);

        assertTrue(rideDetailsModal.isOpened());
        assertTrue(rideDetailsModal.isRatingButtonDisplayed(), "Leave rating button should be displayed");
        rideDetailsModal.clickLeaveRating();

        assertTrue(rideRatingPage.isOpened());
        assertFalse(rideRatingPage.isRatingExpired(), "Rating period should not be expired");

        rideRatingPage.submitRating(driverRating, vehicleRating, comment);

        assertTrue(rideRatingPage.isSuccessMessageDisplayed(), "Success message should be displayed");
    }

    @Test(dataProvider = "minimumRatingData")
    public void testRateRideWithMinimumData(
            String email, String password,
            int rideIndex,
            int driverRating,
            int vehicleRating,
            String comment
    ) {
        assertTrue(unregisteredHomePage.isOpened());
        unregisteredHomePage.clickLoginButton();

        assertTrue(loginPage.isOpened());
        loginPage.login(email, password);

        navigationBar.openRideHistory();

        assertTrue(rideHistoryPage.isOpened());
        rideHistoryPage.viewDetailsByRideIndex(rideIndex);

        assertTrue(rideDetailsModal.isOpened());
        assertTrue(rideDetailsModal.isRatingButtonDisplayed(), "Leave rating button should be displayed");
        rideDetailsModal.clickLeaveRating();

        assertTrue(rideRatingPage.isOpened());
        assertFalse(rideRatingPage.isRatingExpired(), "Rating period should not be expired");

        rideRatingPage.submitRating(driverRating, vehicleRating, comment);

        assertTrue(rideRatingPage.isSuccessMessageDisplayed(), "Success message should be displayed");
    }

    @Test(dataProvider = "expiredRatingData")
    public void testRateRideAfterExpirationDate(
            String email, String password,
            String startDay, String startMonth, String startYear,
            String endDay, String endMonth, String endYear,
            int rideIndex
    ) {
        assertTrue(unregisteredHomePage.isOpened());
        unregisteredHomePage.clickLoginButton();

        assertTrue(loginPage.isOpened());
        loginPage.login(email, password);

        navigationBar.openRideHistory();

        assertTrue(rideHistoryPage.isOpened());
        rideHistoryPage.filterRides(startDay, startMonth, startYear, endDay, endMonth, endYear);
        rideHistoryPage.viewDetailsByRideIndex(rideIndex);

        assertTrue(rideDetailsModal.isOpened());
        assertFalse(rideDetailsModal.isRatingButtonDisplayed(), "Leave rating button should NOT be displayed for expired rides");
    }
}