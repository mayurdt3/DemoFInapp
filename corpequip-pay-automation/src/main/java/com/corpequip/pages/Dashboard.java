package com.corpequip.pages;

import com.corpequip.base.BasePage;
import com.corpequip.exceptions.ElementException;
import com.corpequip.exceptions.NotFoundException;
import com.corpequip.exceptions.ValidationException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Dashboard page - entry point for the application.
 * URL: #/dashboard or #/ (default route)
 * Contains Business Overview section with metrics.
 */
public class Dashboard extends BasePage {

    private static final Logger LOG = Logger.getLogger(Dashboard.class.getName());

    // ============================================
    // LOCATORS - All page elements defined here
    // ============================================

    @FindBy(css = "[data-testid='overview-select']")
    private WebElement overviewSelect;

    @FindBy(css = "[data-testid='overview-label']")
    private WebElement overviewLabel;

    @FindBy(css = "[data-testid='nav-reports']")
    private WebElement navReports;

    // Business Overview items - generated dynamically with data-testid="overview-{key}"
    // These are accessed via helper method since they're dynamically generated

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public Dashboard(WebDriver driver) {
        super(driver);
        try {
            // Wait for dashboard to load
            this.wait.until(ExpectedConditions.visibilityOf(overviewSelect));
            LOG.fine("Dashboard page loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Dashboard page", e);
            throw new ElementException("Dashboard page failed to load: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE ACTIONS
    // ============================================

    /**
     * Select year from the overview date selection dropdown.
     * @param year the year or period string to select (e.g., "2026", "2025", "All", or "January 2026")
     * @return this page instance for method chaining
     * @throws ElementException if the dropdown element is not interactable
     */
    public Dashboard selectOverviewYear(String year) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(overviewSelect));
            Select selectElement = new Select(overviewSelect);
            selectElement.selectByVisibleText(year);
            LOG.fine("Selected year: " + year);
            return this;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to select year: " + year, e);
            throw new ElementException("Failed to select year '" + year + "': " + e.getMessage(), e);
        }
    }

    /**
     * Get the visible label for the current overview period.
     * @return the period label text
     * @throws ElementException if the label element is not found or not visible
     */
    public String getOverviewLabel() {
        try {
            wait.until(ExpectedConditions.visibilityOf(overviewLabel));
            String label = overviewLabel.getText().trim();
            LOG.fine("Overview label: " + label);
            return label;
        } catch (ElementException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get overview label", e);
            throw new ElementException("Failed to get overview label: " + e.getMessage(), e);
        }
    }

    /**
     * Get the Batch Total from Business Overview.
     * This should equal the Totals Summary Total Submitted Amount when bug is OFF.
     * Bug-01 causes Totals Summary to show gross sales instead of net (sales - refunds).
     * @return the batch total as a string
     * @throws NotFoundException if the batch total element is not found
     * @throws ValidationException if the element text is empty or invalid
     */
    public String getBatchTotal() {
        try {
            WebElement batchTotalElement = getOverviewItem("batch-total");
            wait.until(ExpectedConditions.visibilityOf(batchTotalElement));
            String batchTotal = batchTotalElement.getText().trim();

            if (batchTotal == null || batchTotal.isEmpty()) {
                throw new ValidationException("Batch total value is empty");
            }

            LOG.fine("Batch Total: " + batchTotal);
            return batchTotal;
        } catch (NotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get batch total", e);
            throw new NotFoundException("Failed to get batch total: " + e.getMessage(), e);
        }
    }

    /**
     * Get the count of transactions from Business Overview.
     * @return the transaction count as an integer
     * @throws NotFoundException if the count element is not found
     * @throws ValidationException if the count cannot be parsed as an integer
     */
    public int getTransactionCount() {
        try {
            WebElement countElement = getOverviewItem("count");
            wait.until(ExpectedConditions.visibilityOf(countElement));
            String countText = countElement.getText().trim();
            int count = Integer.parseInt(countText.replace(",", ""));
            LOG.fine("Transaction count: " + count);
            return count;
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Failed to parse transaction count: " + e.getMessage());
            throw new ValidationException("Transaction count is not a valid number: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get transaction count", e);
            throw new NotFoundException("Failed to get transaction count: " + e.getMessage(), e);
        }
    }

    /**
     * Get the average ticket amount from Business Overview.
     * @return the average ticket amount as a string
     * @throws NotFoundException if the average element is not found
     */
    public String getAverageTicket() {
        try {
            WebElement averageElement = getOverviewItem("average");
            wait.until(ExpectedConditions.visibilityOf(averageElement));
            String average = averageElement.getText().trim();
            LOG.fine("Average ticket: " + average);
            return average;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get average ticket", e);
            throw new NotFoundException("Failed to get average ticket: " + e.getMessage(), e);
        }
    }

    /**
     * Get the busiest day of the week from Business Overview.
     * @return the busiest day as a string
     * @throws NotFoundException if the busiest element is not found
     */
    public String getBusiestDay() {
        try {
            WebElement busiestElement = getOverviewItem("busiest");
            wait.until(ExpectedConditions.visibilityOf(busiestElement));
            String busiest = busiestElement.getText().trim();
            LOG.fine("Busiest day: " + busiest);
            return busiest;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get busiest day", e);
            throw new NotFoundException("Failed to get busiest day: " + e.getMessage(), e);
        }
    }

    /**
     * Get total chargebacks from Business Overview.
     * @return the chargebacks amount as a string
     * @throws NotFoundException if the chargebacks element is not found
     */
    public String getChargebacks() {
        try {
            WebElement chargebacksElement = getOverviewItem("chargebacks");
            wait.until(ExpectedConditions.visibilityOf(chargebacksElement));
            String chargebacks = chargebacksElement.getText().trim();
            LOG.fine("Chargebacks: " + chargebacks);
            return chargebacks;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get chargebacks", e);
            throw new NotFoundException("Failed to get chargebacks: " + e.getMessage(), e);
        }
    }

    /**
     * Navigate to Reports Hub.
     * @return new ReportsHubPage instance
     * @throws ElementException if navigation fails
     */
    public ReportsHubPage navigateToReports() {
        try {
            click(navReports);
            return new ReportsHubPage(driver);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to navigate to Reports Hub", e);
            throw new ElementException("Failed to navigate to Reports Hub: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    /**
     * Get dynamically generated overview item by key.
     * @param key the data-testid key suffix
     * @return the WebElement for the overview item
     * @throws NotFoundException if the element is not found
     */
    private WebElement getOverviewItem(String key) {
        try {
            By locator = By.cssSelector("[data-testid='overview-" + key + "']");
            WebElement element = driver.findElement(locator);
            return element;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            LOG.warning("Overview item not found: " + key);
            throw new NotFoundException("Overview item '" + key + "' not found: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error finding overview item: " + key, e);
            throw new ElementException("Error finding overview item '" + key + "': " + e.getMessage(), e);
        }
    }
}