package com.tricolori.tests;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tricolori.pages.LoginPage;
import com.tricolori.pages.PassengerNavigation;
import com.tricolori.pages.RideHistoryPassengerPage;
import com.tricolori.pages.RideRatingPage;
import com.tricolori.pages.UnregisteredHomePage;

class RateRideThroughRideHistory {
    private WebDriver driver;

    private UnregisteredHomePage unregisteredHomePage;
    private LoginPage loginPage;
    private PassengerNavigation navigationBar;
    private RideHistoryPassengerPage rideHistoryPage;
    private RideRatingPage rideRatingPage;

    private final static String PATH = "http://localhost:4200/";

    @BeforeSuite
    public void initialize() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--incognito", "--lang=sr-RS");

        this.driver = new ChromeDriver(options);
        this.driver.get(PATH);

        this.unregisteredHomePage = new UnregisteredHomePage(this.driver);
        this.loginPage = new LoginPage(this.driver);
        this.navigationBar = new PassengerNavigation(this.driver);
        this.rideHistoryPage = new RideHistoryPassengerPage(this.driver);
        this.rideRatingPage = new RideRatingPage(this.driver);
    }

    @AfterSuite
    public void uninitialize() {
        this.driver.quit();
    }

    @DataProvider
    Object[][] ratingData() {
        return new Object[][] {
                {
                        "passenger@test.com",
                        "Password123",
                        "11", "02", "2026",
                        "15", "02", "2026",
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
                        "passenger@test.com",
                        "Password123",
                        0,
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
                        "passenger@test.com",
                        "Password123",
                        "01", "01", "2026",
                        "10", "01", "2026",
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
        rideHistoryPage.rateRideByIndex(rideIndex);

        assertTrue(rideRatingPage.isOpened());
        assertTrue(!rideRatingPage.isRatingExpired(), "Rating period should not be expired");

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
        rideHistoryPage.rateRideByIndex(rideIndex);

        assertTrue(rideRatingPage.isOpened());
        assertTrue(!rideRatingPage.isRatingExpired(), "Rating period should not be expired");

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
        rideHistoryPage.rateRideByIndex(rideIndex);

        assertTrue(rideRatingPage.isOpened());
        assertTrue(rideRatingPage.isRatingExpired(), "Rating period should be expired for rides older than 3 days");
    }
}