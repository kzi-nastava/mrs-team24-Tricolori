package com.tricolori.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RideBookingPage {
    @FindBy(id = "select-from-favorite-routes-btn")
    private WebElement selectFavoriteRoutesButton;

    @FindBy(id = "book-a-ride-btn")
    private WebElement rideBookButton;

    @FindBy(id = "ride-book-welcome")
    private WebElement welcome;

    @FindBy(id = "passenger-history")
    private WebElement historyButton;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public RideBookingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(welcome)).isDisplayed();
    }

    public void openFavoriteRoutes() {
        wait.until(ExpectedConditions.elementToBeClickable(selectFavoriteRoutesButton));
        actions.moveToElement(selectFavoriteRoutesButton).click().perform();
    }

    public void bookARide() {
        wait.until(ExpectedConditions.elementToBeClickable(rideBookButton));
        actions.moveToElement(rideBookButton).click().perform();
    }

    public void openRideHistory() {
        wait.until(ExpectedConditions.elementToBeClickable(historyButton));
        actions.moveToElement(historyButton).click().perform();
    }
}
