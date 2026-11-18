package com.corpequip.pages;

import com.corpequip.base.BasePage;
import com.corpequip.exceptions.ElementException;
import com.corpequip.exceptions.NotFoundException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Daily Discount Calculator Report page.
 * URL: #/reports/discount-calculator
 *
 * Shows batches with their funded amounts after daily discount fee collection.
 * Each day that has batches gets a "DAILY TOTAL" summary row (class 'daily-total').
 *
 * Bug-07: When bug is ON, each day's total shows only the LAST batch amount
 * instead of the sum of all batches that day.
 */
public class DailyDiscountPage extends BasePage {

    private static final Logger LOG = Logger.getLogger(DailyDiscountPage.class.getName());

    // ============================================
    // LOCATORS - All page elements defined here
    // ============================================

    @FindBy(css = "[data-testid='report-period']")
    private WebElement reportPeriodLabel;

    @FindBy(css = "[data-testid='filter-mode']")
    private WebElement filterModeSelect;

    @FindBy(css = "[data-testid='filter-year']")
    private WebElement yearSelect;

    @FindBy(css = "[data-testid='filter-month']")
    private WebElement monthSelect;

    @FindBy(css = "[data-testid='filter-apply']")
    private WebElement applyButton;

    @FindBy(css = "[data-testid='report-table']")
    private WebElement reportTable;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public DailyDiscountPage(WebDriver driver) {
        super(driver);
        try {
            // Wait for report table to load
            this.wait.until(ExpectedConditions.visibilityOf(reportTable));
            LOG.fine("Daily Discount Calculator page loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Daily Discount Calculator page", e);
            throw new ElementException("Daily Discount Calculator page failed to load: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE ACTIONS
    // ============================================

    /**
     * Set the report filter to By Month for a specific month and year.
     *
     * @param month the month number as string (e.g., "10" for October)
     * @param year  the year to filter by (e.g., "2026")
     * @return this page instance for method chaining
     * @throws ElementException if filter operation fails
     */
    public DailyDiscountPage filterByMonth(String month, String year) {
        try {
            // Click the mode dropdown
            click(filterModeSelect);
            selectByText(filterModeSelect, "By Month");

            // Select the month by value (1-12)
            selectByValue(monthSelect, month);

            // Select the year
            selectByText(yearSelect, year);

            // Apply the filter
            click(applyButton);

            // Wait for table to refresh
            this.wait.until(ExpectedConditions.visibilityOf(reportTable));
            LOG.fine("Filtered Daily Discount Calculator by month: " + month + ", year: " + year);
            return this;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to filter Daily Discount Calculator by month/year", e);
            throw new ElementException(
                    "Failed to filter by month '" + month + "' and year '" + year + "': " + e.getMessage(), e);
        }
    }

    /**
     * Get the report period label.
     *
     * @return the period label text
     * @throws NotFoundException if the label is not found
     */
    public String getReportPeriod() {
        try {
            wait.until(ExpectedConditions.visibilityOf(reportPeriodLabel));
            String period = reportPeriodLabel.getText().trim();
            LOG.fine("Report period: " + period);
            return period;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get report period", e);
            throw new NotFoundException("Failed to get report period: " + e.getMessage(), e);
        }
    }
}