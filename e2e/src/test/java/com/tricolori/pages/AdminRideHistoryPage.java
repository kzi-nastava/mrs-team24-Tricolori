package com.tricolori.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AdminRideHistoryPage {

    // PAGE HEADER
    @FindBy(id = "page-title")
    private WebElement pageTitle;

    @FindBy(id = "loading-spinner")
    private WebElement loadingSpinner;

    // FILTERS
    @FindBy(id = "email-filter")
    private WebElement emailFilterInput;

    @FindBy(id = "start-date-filter")
    private WebElement startDateInput;

    @FindBy(id = "end-date-filter")
    private WebElement endDateInput;

    @FindBy(id = "apply-filters-btn")
    private WebElement applyFiltersButton;

    @FindBy(id = "clear-filters-btn")
    private WebElement clearFiltersButton;

    // RIDES LIST
    @FindBy(id = "rides-list")
    private WebElement ridesListContainer;

    @FindBy(css = "#rides-list > div[id^='ride-row-']")
    private List<WebElement> rideRows;

    @FindBy(id = "no-rides-message")
    private WebElement noRidesMessage;

    // PAGINATION
    @FindBy(id = "prev-page-btn")
    private WebElement prevPageButton;

    @FindBy(id = "next-page-btn")
    private WebElement nextPageButton;

    @FindBy(id = "current-page-indicator")
    private WebElement pageIndicator;

    // MODAL
    @FindBy(id = "ride-details-modal")
    private WebElement rideDetailsModal;

    @FindBy(id = "sort-date")
    private WebElement sortDateButton;

    @FindBy(id = "sort-price")
    private WebElement sortPriceButton;

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    public AdminRideHistoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.actions = new Actions(driver);

        PageFactory.initElements(driver, this);
    }

    // PAGE STATE

    public boolean isOpened() {
        return wait.until(ExpectedConditions.visibilityOf(pageTitle)).isDisplayed();
    }

    public void waitForLoadingToFinish() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-spinner")));
    }

    public void clickSortByDate() {

        wait.until(ExpectedConditions.elementToBeClickable(sortDateButton));

        actions.moveToElement(sortDateButton).click().perform();

        waitForLoadingToFinish();
    }

    public void clickSortByPrice() {

        wait.until(ExpectedConditions.elementToBeClickable(sortPriceButton));

        actions.moveToElement(sortPriceButton).click().perform();

        waitForLoadingToFinish();
    }


    // FILTER ACTIONS

    public void filterByEmail(String email) {
        wait.until(ExpectedConditions.elementToBeClickable(emailFilterInput));

        emailFilterInput.click();
        emailFilterInput.clear();
        emailFilterInput.sendKeys(email);

        clickApplyFilters();
    }

    public void filterByDate(
            String startDay, String startMonth, String startYear,
            String endDay, String endMonth, String endYear) {

        fillDateInput(startDateInput, startDay, startMonth, startYear);

        fillDateInput(endDateInput, endDay, endMonth, endYear);

        clickApplyFilters();
    }


    public void clearFilters() {
        wait.until(ExpectedConditions.elementToBeClickable(clearFiltersButton));
        actions.moveToElement(clearFiltersButton).click().perform();

        waitForLoadingToFinish();
    }

    private void clickApplyFilters() {
        wait.until(ExpectedConditions.elementToBeClickable(applyFiltersButton));
        actions.moveToElement(applyFiltersButton).click().perform();

        waitForLoadingToFinish();
    }

    private void fillDateInput(WebElement inputElement, String day, String month, String year) {
        wait.until(ExpectedConditions.elementToBeClickable(inputElement));

        actions.moveToElement(inputElement).click();

        for (int i = 0; i < 8; i++) {
            actions.sendKeys(Keys.BACK_SPACE);
        }

        actions.sendKeys(month + day + year).build().perform();
    }

    // RIDES

    public int getRideCount() {
        waitForLoadingToFinish();

        if (isNoRidesMessageDisplayed()) {
            return 0;
        }

        wait.until(ExpectedConditions.visibilityOfAllElements(rideRows));
        return rideRows.size();
    }

    public boolean isNoRidesMessageDisplayed() {
        try {
            return noRidesMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void viewRideDetailsByIndex(int index) {
        List<WebElement> rides = getRides();

        if (index < 0 || index >= rides.size()) {
            throw new RuntimeException("Invalid ride index");
        }

        WebElement ride = rides.get(index);

        WebElement viewButton = ride.findElement(By.cssSelector("button[id^='view-details-btn-']"));

        wait.until(ExpectedConditions.elementToBeClickable(viewButton));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", viewButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ride-details-modal")));
    }

    private List<WebElement> getRides() {
        waitForLoadingToFinish();

        if (isNoRidesMessageDisplayed()) {
            throw new RuntimeException("No rides found");
        }

        wait.until(ExpectedConditions.visibilityOfAllElements(rideRows));
        return rideRows;
    }

    public List<LocalDate> getRideDates() {

        waitForLoadingToFinish();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy");

        return rideRows.stream()
                .map(row -> {

                    WebElement dateElement =
                            row.findElement(By.cssSelector("[id^='ride-date-']"));

                    String text = dateElement.getText().trim();

                    return LocalDate.parse(text, formatter);
                })
                .toList();
    }

    public List<Double> getRidePrices() {

        waitForLoadingToFinish();

        List<Double> prices = new ArrayList<>();
        for (WebElement rideRow : rideRows) {
            WebElement priceEl = rideRow.findElement(By.cssSelector("[id^='ride-price-']"));
            String priceText = priceEl.getText().replaceAll("[^\\d.,]", "");
            priceText = priceText.replace(",", ".");
            prices.add(Double.parseDouble(priceText));
        }
        return prices;
    }


    public boolean isRideDetailsModalOpened() {
        return wait.until(ExpectedConditions.visibilityOf(rideDetailsModal)).isDisplayed();
    }

    // PAGINATION

    public void goToNextPage() {
        wait.until(ExpectedConditions.elementToBeClickable(nextPageButton));
        actions.moveToElement(nextPageButton).click().perform();

        waitForLoadingToFinish();
    }

    public void goToPreviousPage() {
        wait.until(ExpectedConditions.elementToBeClickable(prevPageButton));
        actions.moveToElement(prevPageButton).click().perform();

        waitForLoadingToFinish();
    }

    public String getCurrentPageText() {
        wait.until(ExpectedConditions.visibilityOf(pageIndicator));
        return pageIndicator.getText();
    }
}
