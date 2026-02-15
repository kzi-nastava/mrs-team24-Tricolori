package com.tricolori.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
    @FindBy(id = "email-input")
    private WebElement emailInput;

    @FindBy(id = "password-input")
    private WebElement passwordInput;

    @FindBy(id = "login-submit")
    private WebElement loginButton;

    @FindBy(id = "login-page-welcome")
    private WebElement welcome;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(welcome)).isDisplayed();
    }

    public void login(String email, String password) {
        // Entering passenger email...
        this.wait.until(ExpectedConditions.elementToBeClickable(emailInput));
        this.actions.moveToElement(emailInput).click().sendKeys(email).perform();
        
        // Entering passenger password...
        this.wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
        this.actions.moveToElement(passwordInput).click().sendKeys(password).perform();

        // Logging in...
        this.wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        this.actions.moveToElement(loginButton).click().perform();
    }
}
