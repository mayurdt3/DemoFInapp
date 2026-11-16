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
 * Reports Hub page - entry point for all reports.
 * URL: #/reports
 */
public class ReportsHubPage extends BasePage {

    private static final Logger LOG = Logger.getLogger(ReportsHubPage.class.getName());

    // ============================================
    // LOCATORS - All page elements defined here
    // ============================================

    @FindBy(css = "[data-testid='view-report-totals-summary']")
    private WebElement totalsSummaryLink;

    @FindBy(css = "[data-testid='view-report-settled-batches']")
    private WebElement settledBatchesLink;

    @FindBy(css = "[data-testid='view-report-search-transactions']")
    private WebElement searchTransactionsLink;

    @FindBy(css = "[data-testid='report-period']")
    private WebElement reportPeriodLabel;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public ReportsHubPage(WebDriver driver) {
        super(driver);
        try {
            // Wait for reports hub to load
            this.wait.until(ExpectedConditions.visibilityOf(totalsSummaryLink));
            LOG.fine("Reports Hub page loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Reports Hub page", e);
            throw new ElementException("Reports Hub page failed to load: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE ACTIONS
    // ============================================

    /**
     * Navigate to Totals Summary Report page.
     * @return new TotalsSummaryPage instance
     * @throws ElementException if navigation fails
     */
    public TotalsSummaryPage navigateToTotalsSummary() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(totalsSummaryLink));
            click(totalsSummaryLink);
            return new TotalsSummaryPage(driver);
        } catch (ElementException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to navigate to Totals Summary", e);
            throw new ElementException("Failed to navigate to Totals Summary: " + e.getMessage(), e);
        }
    }

    /**
     * Navigate to Settled Batches Report page.
     * @return new SettledBatchesPage instance
     * @throws ElementException if navigation fails
     */
    public SettledBatchesPage navigateToSettledBatches() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(settledBatchesLink));
            click(settledBatchesLink);
            return new SettledBatchesPage(driver);
        } catch (ElementException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to navigate to Settled Batches", e);
            throw new ElementException("Failed to navigate to Settled Batches: " + e.getMessage(), e);
        }
    }

    /**
     * Navigate to Search Transactions Report page.
     * @return new SearchTransactionsPage instance
     * @throws ElementException if navigation fails
     */
    public SearchTransactionsPage navigateToSearchTransactions() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(searchTransactionsLink));
            click(searchTransactionsLink);
            return new SearchTransactionsPage(driver);
        } catch (ElementException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to navigate to Search Transactions", e);
            throw new ElementException("Failed to navigate to Search Transactions: " + e.getMessage(), e);
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