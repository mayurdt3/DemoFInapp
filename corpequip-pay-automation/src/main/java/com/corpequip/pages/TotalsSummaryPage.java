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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Totals Summary Report page.
 * URL: #/reports/totals-summary
 * Used for Scenario 2 to get Total Submitted Amount.
 *
 * Bug-01: When bug is ON, total = sales (gross). When bug is OFF, total = sales
 * - refunds (net).
 */
public class TotalsSummaryPage extends BasePage {

    private static final Logger LOG = Logger.getLogger(TotalsSummaryPage.class.getName());

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

    @FindBy(css = "[data-testid='report-table-totals-row']")
    private WebElement totalsRow;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public TotalsSummaryPage(WebDriver driver) {
        super(driver);
        try {
            // Wait for report table to load
            this.wait.until(ExpectedConditions.visibilityOf(reportTable));
            LOG.fine("Totals Summary page loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Totals Summary page", e);
            throw new ElementException("Totals Summary page failed to load: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE ACTIONS
    // ============================================

    /**
     * Set the report filter to By Year for a specific year.
     *
     * @param year the year to filter by (e.g., "2026")
     * @return this page instance for method chaining
     * @throws ElementException if filter operation fails
     */
    public TotalsSummaryPage filterByYear(String year) {
        try {
            // Click the mode dropdown
            click(filterModeSelect);
            selectByText(filterModeSelect, "By Year");

            // Select the year
            selectByText(yearSelect, year);

            // Apply the filter
            click(applyButton);

            // Wait for table to refresh
            this.wait.until(ExpectedConditions.visibilityOf(totalsRow));
            LOG.fine("Filtered Totals Summary by year: " + year);
            return this;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to filter Totals Summary by year: " + year, e);
            throw new ElementException("Failed to filter by year '" + year + "': " + e.getMessage(), e);
        }
    }

    /**
     * Set the report filter to By Month for a specific month and year.
     *
     * @param month the month number as string (e.g., "1" for January, "2" for February)
     * @param year  the year to filter by (e.g., "2026")
     * @return this page instance for method chaining
     * @throws ElementException if filter operation fails
     */
    public TotalsSummaryPage filterByMonth(String month, String year) {
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
            this.wait.until(ExpectedConditions.visibilityOf(totalsRow));
            LOG.fine("Filtered Totals Summary by month: " + month + ", year: " + year);
            return this;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to filter Totals Summary by month/year", e);
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

    /**
     * Get the Total Submitted Amount from the report table footer.
     * This should equal the Business Overview Batch Total when bug is OFF.
     * Bug-01 causes this to show gross sales instead of net (sales - refunds).
     *
     * @return the total submitted amount as a string
     * @throws NotFoundException   if the totals row is not found
     * @throws ValidationException if the amount cannot be parsed
     */
    public String getTotalSubmittedAmount() {
        try {
            wait.until(ExpectedConditions.visibilityOf(totalsRow));
            String totalText = totalsRow.getText().trim();
            // Parse the dollar amount from the text
            String amount = totalText.contains("$") ? totalText.substring(totalText.lastIndexOf("$")) : totalText;
            LOG.fine("Total Submitted Amount: " + amount);
            return amount;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get total submitted amount", e);
            throw new ElementException("Failed to get total submitted amount: " + e.getMessage(), e);
        }
    }

    /**
     * Get the Total Submitted Amount as a numeric value (in cents).
     * The app shows amounts in dollars like "$1,234.56"
     *
     * @return the amount in cents
     * @throws ValidationException if the amount cannot be parsed
     */
    public long getTotalSubmittedInCents() {
        try {
            wait.until(ExpectedConditions.visibilityOf(totalsRow));
            String amountText = totalsRow.getText().trim();
            // Find the dollar amount in the text
            int dollarIndex = amountText.lastIndexOf("$");
            if (dollarIndex >= 0) {
                amountText = amountText.substring(dollarIndex);
            }
            String cleaned = amountText.replace("$", "").replace(",", "").trim();
            long cents = (long) (Double.parseDouble(cleaned) * 100);
            LOG.fine("Total Submitted in cents: " + cents);
            return cents;
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Failed to parse amount to cents: " + e.getMessage());
            throw new ValidationException("Failed to parse amount to cents: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get total submitted in cents", e);
            throw new ElementException("Failed to get total submitted in cents: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE DATA ACCESS
    // ============================================

    /**
     * Get all rows from the report table.
     *
     * @return list of table row WebElements
     * @throws NotFoundException if the table rows cannot be found
     */
    public List<WebElement> getTableRows() {
        try {
            wait.until(ExpectedConditions.visibilityOf(reportTable));
            List<WebElement> rows = driver.findElements(By.cssSelector("[data-testid='report-table'] tbody tr"));
            LOG.fine("Found " + rows.size() + " table rows");
            return rows;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get table rows", e);
            throw new NotFoundException("Failed to get table rows: " + e.getMessage(), e);
        }
    }

    /**
     * Get the number of rows in the report.
     *
     * @return the row count
     */
    public int getRowCount() {
        try {
            return getTableRows().size();
        } catch (NotFoundException e) {
            LOG.warning("getRowCount called but table rows not found, returning 0");
            return 0;
        }
    }
}