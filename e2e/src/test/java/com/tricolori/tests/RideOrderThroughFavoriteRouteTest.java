package com.tricolori.tests;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tricolori.pages.*;
import com.tricolori.utils.RouteSummary;

class RideOrderThroughFavoriteRouteTest {
    private WebDriver driver;

    // Pages:
    private UnregisteredHomePage unregisteredHomePage;
    private LoginPage loginPage;
    private RideBookingPage rideBookingPage;
    private FavoriteRoutesModal favoriteRoutesModal;
    private RideHistoryPassengerPage rideHistoryPage;
    private RideDetailsModal rideDetailsModal;
    private RouteSelectorPart routeSelectorPart;

    private final static String PATH = "http://localhost:4200/";

    @BeforeSuite
    public void initialize() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--incognito", "--lang=sr-RS");

        this.driver = new ChromeDriver(options);
        this.driver.get(PATH);

        this.unregisteredHomePage = new UnregisteredHomePage(this.driver);
        this.loginPage = new LoginPage(this.driver);
        this.rideBookingPage = new RideBookingPage(this.driver);
        this.favoriteRoutesModal = new FavoriteRoutesModal(this.driver);
        this.rideHistoryPage = new RideHistoryPassengerPage(this.driver);
        this.rideDetailsModal = new RideDetailsModal(this.driver);
        this.routeSelectorPart = new RouteSelectorPart(this.driver);
    }

    @AfterSuite
    public void uninitialize() {
        this.driver.quit();
    }

    @DataProvider
    Object[][] history() {
        return new Object[][] {
            { 
                "aleksa.curcic.uni@gmail.com", 
                "Password123",
                "11", "02", "2026",
                "11", "02", "2026",
                0
            }
        };
    }

    @DataProvider
    Object[][] ordering() {
        return new Object[][] {
            { "aleksa.curcic.uni@gmail.com", "Password123", 0 }
        };
    }


    @Test(dataProvider = "history", priority = 1)
    public void testRemoveRouteFromFavorites(
        String email, String password,
        String startDay, String startMonth, String startYear,
        String endDay, String endMonth, String endYear,
        int rideIndex
    ) {
        assertTrue(unregisteredHomePage.isOpened());
        unregisteredHomePage.clickLoginButton();

        assertTrue(loginPage.isOpened());
        loginPage.login(email, password);

        assertTrue(rideBookingPage.isOpened());
        rideBookingPage.openRideHistory();

        assertTrue(rideHistoryPage.isOpened());
        rideHistoryPage.filterRides(startDay, startMonth, startYear, endDay, endMonth, endYear);
        rideHistoryPage.viewDetailsByRideIndex(rideIndex);

        assertTrue(rideDetailsModal.isOpened());
        assertTrue(rideDetailsModal.isFavorite(), "Route should be in favorites.");

        rideDetailsModal.removeFromFavorites();
    }

    
    @Test(dataProvider = "history", priority = 2)
    public void testAddRouteToFavorites(
        String email, String password,
        String startDay, String startMonth, String startYear,
        String endDay, String endMonth, String endYear,
        int rideIndex
    ) {
        assertTrue(unregisteredHomePage.isOpened());
        unregisteredHomePage.clickLoginButton();

        assertTrue(loginPage.isOpened());
        loginPage.login(email, password);

        assertTrue(rideBookingPage.isOpened());
        rideBookingPage.openRideHistory();

        assertTrue(rideHistoryPage.isOpened());
        rideHistoryPage.filterRides(startDay, startMonth, startYear, endDay, endMonth, endYear);
        rideHistoryPage.viewDetailsByRideIndex(rideIndex);

        assertTrue(rideDetailsModal.isOpened());
        assertTrue(!rideDetailsModal.isFavorite(), "Route shouldn't already be in favorites.");

        rideDetailsModal.addToFavorite();
    }


    @Test(dataProvider = "ordering", priority = 3)
    public void testRideOrderFromExistingFavoriteRoute(String email, String password, int routeIndex) {
        assertTrue(unregisteredHomePage.isOpened());
        unregisteredHomePage.clickLoginButton();

        assertTrue(loginPage.isOpened());
        loginPage.login(email, password);

        assertTrue(rideBookingPage.isOpened());
        rideBookingPage.openFavoriteRoutes();

        assertTrue(favoriteRoutesModal.isOpened());
        RouteSummary summary = favoriteRoutesModal.selectRouteByIndex(routeIndex);
        
        assertNotNull(summary.from);
        assertNotNull(summary.to);

        assertTrue(routeSelectorPart.isOpened());
        assertTrue(routeSelectorPart.isFilledWithRoute(summary));
    }


}