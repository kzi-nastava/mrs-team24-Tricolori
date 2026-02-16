package com.tricolori.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminNavbar {
    private final WebDriverWait wait;

    public AdminNavbar(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void goToHistory() {
        WebElement historyLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("admin-nav-history")));
        historyLink.click();
        wait.until(ExpectedConditions.urlContains("/admin/history"));
    }

    public boolean isNavbarVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("admin-nav-home"))).isDisplayed();
        } catch (Exception e) { return false; }
    }
}