package com.tricolori.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RideRatingPage {
    @FindBy(id = "rating-page-title")
    private WebElement pageTitle;

    @FindBy(css = "div.bg-gray-50.rounded-lg")
    private WebElement rideDetailsSection;

    @FindBy(id = "driver-rating-stars")
    private WebElement driverRatingStars;

    @FindBy(id = "vehicle-rating-stars")
    private WebElement vehicleRatingStars;

    @FindBy(id = "rating-comment")
    private WebElement commentTextarea;

    @FindBy(id = "submit-rating-btn")
    private WebElement submitButton;

    @FindBy(id = "rating-success-message")
    private WebElement successMessage;

    @FindBy(id = "rating-expired-message")
    private WebElement expiredMessage;

    @FindBy(id = "rating-error-message")
    private WebElement errorMessage;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public RideRatingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(pageTitle)).isDisplayed() &&
                wait.until(ExpectedConditions.visibilityOf(rideDetailsSection)).isDisplayed();
    }

    public void setDriverRating(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        wait.until(ExpectedConditions.visibilityOf(driverRatingStars));
        List<WebElement> starsButtons = driverRatingStars.findElements(By.tagName("button"));

        wait.until(ExpectedConditions.elementToBeClickable(starsButtons.get(stars - 1)));
        actions.moveToElement(starsButtons.get(stars - 1)).click().perform();
    }

    public void setVehicleRating(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        wait.until(ExpectedConditions.visibilityOf(vehicleRatingStars));
        List<WebElement> starsButtons = vehicleRatingStars.findElements(By.tagName("button"));

        wait.until(ExpectedConditions.elementToBeClickable(starsButtons.get(stars - 1)));
        actions.moveToElement(starsButtons.get(stars - 1)).click().perform();
    }

    public void enterComment(String comment) {
        wait.until(ExpectedConditions.visibilityOf(commentTextarea));
        actions.moveToElement(commentTextarea).click().sendKeys(comment).perform();
    }

    public void submitRating() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        actions.moveToElement(submitButton).click().perform();
    }

    public void submitRating(int driverRating, int vehicleRating, String comment) {
        setDriverRating(driverRating);
        setVehicleRating(vehicleRating);
        if (comment != null && !comment.isEmpty()) {
            enterComment(comment);
        }
        submitRating();
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRatingExpired() {
        try {
            wait.until(ExpectedConditions.visibilityOf(expiredMessage));
            return expiredMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}