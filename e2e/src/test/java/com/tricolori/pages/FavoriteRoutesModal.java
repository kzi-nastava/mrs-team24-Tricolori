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

import com.tricolori.utils.RouteSummary;


public class FavoriteRoutesModal {
    @FindBy(id = "favorite-route-welcome")
    private WebElement welcome;

    @FindBy(className = "route")
    private List<WebElement> routes;

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    public FavoriteRoutesModal(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.actions = new Actions(this.driver);

        PageFactory.initElements(this.driver, this);
    }

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(welcome)).isDisplayed();
    }

    public RouteSummary selectRouteByIndex(int routeIndex) {
        wait.until(ExpectedConditions.visibilityOfAllElements(routes));

        if (routeIndex >= 0 && routeIndex < routes.size()) {
            WebElement targetRoute = routes.get(routeIndex);

            String from = targetRoute.findElement(By.xpath(".//p[text()='From:']/following-sibling::p")).getText();
            String to = targetRoute.findElement(By.xpath(".//p[text()='To:']/following-sibling::p")).getText();

            RouteSummary summary = new RouteSummary(from, to);

            actions.moveToElement(targetRoute).click().perform();
            wait.until(ExpectedConditions.invisibilityOf(welcome));

            return summary;
        } else {
            throw new RuntimeException("Invalid index.");
        }
    }
}
