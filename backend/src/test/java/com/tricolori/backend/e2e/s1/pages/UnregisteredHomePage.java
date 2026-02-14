package com.tricolori.backend.e2e.s1.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UnregisteredHomePage {
    @FindBy(id = "login-btn")
    private WebElement loginButton;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public UnregisteredHomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(loginButton)).isDisplayed();
    }

    public void clickLoginButton() {
        this.wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        this.actions.moveToElement(loginButton).click().perform();
    }
}
