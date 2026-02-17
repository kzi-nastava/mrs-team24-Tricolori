package com.tricolori.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PassengerNavigation {
    @FindBy(id = "passenger-history")
    private WebElement historyLink;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public PassengerNavigation(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(this.driver, this);
    }

    public void openRideHistory() {
        this.wait.until(ExpectedConditions.elementToBeClickable(historyLink));
        this.actions.moveToElement(historyLink).click().perform();
    }
}