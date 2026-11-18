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
 * ACH Deposit Report page.
 * URL: #/reports/ach-deposit
 */
public class AchDepositPage extends BasePage {

    private static final Logger LOG = Logger.getLogger(AchDepositPage.class.getName());

    // ============================================
    // LOCATORS - All page elements defined here
    // ============================================

    @FindBy(css = "[data-testid='report-period']")
    private WebElement reportPeriodLabel;

    @FindBy(css = "[data-testid='filter-tranCode']")
    private WebElement tranCodeSelect;

    @FindBy(css = "[data-testid='filter-year']")
    private WebElement yearSelect;

    @FindBy(css = "[data-testid='filter-apply']")
    private WebElement applyButton;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public AchDepositPage(WebDriver driver) {
        super(driver);
        try {
            // Wait for report table to load
            this.wait.until(ExpectedConditions.visibilityOf(reportPeriodLabel));
            LOG.fine("ACH Deposit page loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load ACH Deposit page", e);
            throw new ElementException("ACH Deposit page failed to load: " + e.getMessage(), e);
        }
    }

    // ============================================
    // PAGE ACTIONS
    // ============================================

    /**
     * Filter the report by tranCode and year.
     *
     * @param tranCode the transaction code (e.g., "Checking Deposit")
     * @param year     the year to filter by (e.g., "2026")
     * @return this page instance for method chaining
     * @throws ElementException if filter operation fails
     */
    public AchDepositPage filterByTranCodeAndYear(String tranCode, String year) {
        try {
            // Select tranCode
            if (tranCode != null && !tranCode.isEmpty()) {
                selectByText(tranCodeSelect, tranCode);
            }

            // Select year
            selectByText(yearSelect, year);

            // Apply the filter
            click(applyButton);

            // Wait for table to refresh
            wait.until(ExpectedConditions.visibilityOf(reportPeriodLabel));
            LOG.fine("Filtered ACH Deposit by tranCode: " + tranCode + ", year: " + year);
            return this;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to filter ACH Deposit by tranCode/year", e);
            throw new ElementException(
                    "Failed to filter by tranCode '" + tranCode + "' and year '" + year + "': " + e.getMessage(), e);
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