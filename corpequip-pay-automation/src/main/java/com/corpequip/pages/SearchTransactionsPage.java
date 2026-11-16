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
 * Search Transactions Report page.
 * URL: #/reports/search-transactions
 */
public class SearchTransactionsPage extends BasePage {

    private static final Logger LOG = Logger.getLogger(SearchTransactionsPage.class.getName());

    // ============================================
    // LOCATORS - All page elements defined here
    // ============================================

    @FindBy(css = "[data-testid='report-period']")
    private WebElement reportPeriodLabel;

    @FindBy(css = "[data-testid='filter-year']")
    private WebElement yearSelect;

    @FindBy(css = "[data-testid='filter-apply']")
    private WebElement applyButton;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public SearchTransactionsPage(WebDriver driver) {
        super(driver);
        try {
            // Wait for report table to load
            this.wait.until(ExpectedConditions.visibilityOf(reportPeriodLabel));
            LOG.fine("Search Transactions page loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Search Transactions page", e);
            throw new ElementException("Search Transactions page failed to load: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE ACTIONS
    // ============================================

    /**
     * Filter the report by year.
     * @param year the year to filter by (e.g., "2026")
     * @return this page instance for method chaining
     * @throws ElementException if filter operation fails
     */
    public SearchTransactionsPage filterByYear(String year) {
        try {
            selectByText(yearSelect, year);
            click(applyButton);
            wait.until(ExpectedConditions.visibilityOf(reportPeriodLabel));
            LOG.fine("Filtered Search Transactions by year: " + year);
            return this;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to filter Search Transactions by year: " + year, e);
            throw new ElementException("Failed to filter by year '" + year + "': " + e.getMessage(), e);
        }
    }

    /**
     * Get the report period label.
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