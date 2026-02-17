package com.tricolori.pages;

import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RideDetailsModal {
    @FindBy(id = "add-remove-favorite")
    private WebElement addOrRemoveButton;

    @FindBy(id = "ride-details-welcome")
    private WebElement welcome;

    @FindBy(id = "ride-map")
    private WebElement rideMap;

    @FindBy(css = "button.bg-teal-600.hover\\:bg-teal-700")
    private WebElement leaveRatingButton;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public RideDetailsModal(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);
        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        boolean isWelcomeVisible = wait.until(ExpectedConditions.visibilityOf(welcome)).isDisplayed();
        boolean isMapLoaded = wait.until(ExpectedConditions.visibilityOf(rideMap)).isDisplayed();

        return isWelcomeVisible && isMapLoaded;
    }

    private String getButtonText() {
        return (String) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].textContent;", addOrRemoveButton);
    }

    public void addToFavorite() {
        if (isFavorite()) {
            throw new IllegalStateException("Greška: Vožnja je već u omiljenima!");
        }

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", addOrRemoveButton);

        wait.until(d -> getButtonText().toLowerCase().contains("remove"));
    }

    public void removeFromFavorites() {
        if (!isFavorite()) {
            throw new IllegalStateException("Greška: Vožnja nije u omiljenima!");
        }

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", addOrRemoveButton);

        wait.until(d -> getButtonText().toLowerCase().contains("add"));
    }

    public boolean isFavorite() {
        wait.until(ExpectedConditions.visibilityOf(addOrRemoveButton));
        String text = getButtonText();
        return text != null && text.toLowerCase().contains("remove");
    }

    public void clickLeaveRating() {
        wait.until(ExpectedConditions.elementToBeClickable(leaveRatingButton));
        actions.moveToElement(leaveRatingButton).click().perform();
    }

    public boolean isRatingButtonDisplayed() {
        try {
            return leaveRatingButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}