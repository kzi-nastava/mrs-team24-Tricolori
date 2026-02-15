package com.tricolori.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RideHistoryPassengerPage {
    @FindBy(id = "ride-history-welcome")
    private WebElement welcome;

    @FindBy(className = "ride")
    private List<WebElement> rideRows;

    @FindBy(id = "loading-spinner")
    private WebElement loadingSpinner;

    @FindBy(id = "start-date-input")
    private WebElement startDateInput;

    @FindBy(id = "end-date-input")
    private WebElement endDateInput;

    @FindBy(id = "apply-filter-btn")
    private WebElement filterButton;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public RideHistoryPassengerPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(welcome)).isDisplayed();
    }

    public void filterRides(
        String startDay, String startMonth, String startYear,
        String endDay, String endMonth, String endYear
    ) {
        fillDateInput(startDateInput, startDay, startMonth, startYear);
        fillDateInput(endDateInput, endDay, endMonth, endYear);

        wait.until(ExpectedConditions.elementToBeClickable(filterButton));
        actions.moveToElement(filterButton).click().perform();
    }

    public void viewDetailsByRideIndex(int rideIndex) {
        List<WebElement> allRides = getRides();
        
        if (rideIndex >= 0 && rideIndex < allRides.size()) {
            WebElement targetRide = allRides.get(rideIndex);
            WebElement viewButton = targetRide.findElement(By.className("view-btn"));
            actions.moveToElement(viewButton).click().perform();
        } else {
            throw new RuntimeException("Invalid index");
        }
    }

    private List<WebElement> getRides() {
        wait.until(ExpectedConditions.invisibilityOf(loadingSpinner));
        wait.until(ExpectedConditions.visibilityOfAllElements(rideRows));
        return rideRows;
    }

    private void fillDateInput(WebElement inputElement, String day, String month, String year) {
        wait.until(ExpectedConditions.elementToBeClickable(inputElement));

        actions.moveToElement(inputElement).click();

        for (int i = 0; i < 8; i++) {
            actions.sendKeys(Keys.BACK_SPACE);
        }

        // build().perform() is neccessary at the end of a chain...
        actions.sendKeys(day + month + year).build().perform();
    }
}
