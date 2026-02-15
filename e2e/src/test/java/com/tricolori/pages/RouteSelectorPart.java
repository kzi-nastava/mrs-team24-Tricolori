package com.tricolori.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tricolori.utils.RouteSummary;


public class RouteSelectorPart {
    @FindBy(id = "pickup-input")
    private WebElement pickupInput;

    @FindBy(id = "destination-input")
    private WebElement destinationInput;

    @FindBy(id = "route-selector-welcome")
    private WebElement welcome;

    private WebDriver driver;
    private WebDriverWait wait;

    public RouteSelectorPart(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));

        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(welcome)).isDisplayed();
    }

    public boolean isFilledWithRoute(RouteSummary summary) {
        wait.until(ExpectedConditions.visibilityOf(pickupInput));
        wait.until(ExpectedConditions.visibilityOf(destinationInput));

        String currentPickup = getPickupValue();
        String currentDestination = getDestinationValue();

        boolean pickupMatches = currentPickup != null && currentPickup.trim().equalsIgnoreCase(summary.from.trim());
        boolean destinationMatches = currentDestination != null && currentDestination.trim().equalsIgnoreCase(summary.to.trim());

        return pickupMatches && destinationMatches;
    }

    public String getPickupValue() {
        return pickupInput.getAttribute("value");
    }

    public String getDestinationValue() {
        return destinationInput.getAttribute("value");
    }
}
