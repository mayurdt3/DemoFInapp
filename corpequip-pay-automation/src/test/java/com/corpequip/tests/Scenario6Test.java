package com.corpequip.tests;

import com.corpequip.base.DriverFactory;
import com.corpequip.pages.Dashboard;
import com.corpequip.pages.ReportsHubPage;
import com.corpequip.pages.SettledBatchesPage;
import com.corpequip.pages.DailyDiscountPage;
import com.corpequip.listeners.RetryAnalyzer;
import com.corpequip.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario 6: Daily Discount Calculator - Reconciliation R4
 *
 * Business Rule (from Reconciliation R4):
 * "Daily Discount Calculator daily totals = Σ batch amounts per day"
 *
 * This validates that the DAILY TOTAL rows in the Daily Discount Calculator
 * correctly sum up all batch amounts for each day.
 *
 * When BUG-07 is ON:
 * - Each day's "DAILY TOTAL" shows only the LAST batch amount (not the sum)
 * - Sum of DAILY TOTAL rows will be LESS than the sum of all batch amounts
 *
 * When BUG-07 is OFF:
 * - Each day's "DAILY TOTAL" correctly shows sum of all batches for that day
 * - Sum of DAILY TOTAL rows will equal sum of all batch amounts for the month
 *
 * Test Approach:
 * For a specific month (using last full month from data - October 2026):
 * 1. Navigate to Settled Batches → filter by month → sum "Batch Amount" column
 * 2. Navigate to Daily Discount Calculator → filter by same month → sum "DAILY TOTAL" funded amounts
 * 3. Compare both values
 *
 * Expected when BUG-07 is OFF: Both sums should match exactly
 * When BUG-07 is ON: Daily Discount daily totals sum will be less (only last batch per day counted)
 */
public class Scenario6Test extends BaseTest {

    private static final Logger LOG = LogManager.getLogger(Scenario6Test.class);
    private static final String TEST_YEAR = "2026";
    private static final String TEST_MONTH = "10"; // October 2026 (last full month in data)

    @Override
    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver(ConfigReader.get("browser"), ConfigReader.getBoolean("headless"));
        driver.get(ConfigReader.get("base.url") + "#/dashboard");
    }

    @Override
    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    @Test(description = "Scenario 6: Daily Discount Calculator - Reconciliation R4 (BUG-07 active)",
          retryAnalyzer = RetryAnalyzer.class,
          groups = {"smoke", "regression"})
    public void scenario6_dailyDiscountReconciliation() {
        LOG.info("=== Scenario 6: Daily Discount Calculator (Reconciliation R4) ===");
        LOG.info("Testing Year: {}, Month: {}", TEST_YEAR, TEST_MONTH);

        // Navigate to Dashboard
        Dashboard dashboard = new Dashboard(driver);
        ReportsHubPage reportsHub = dashboard.navigateToReports();

        // Evidence collection
        List<String> evidence = new ArrayList<>();
        evidence.add("Test Year: " + TEST_YEAR);
        evidence.add("Test Month: " + TEST_MONTH);
        evidence.add("Reconciliation: R4 - Daily Discount Calculator daily totals = Σ batch amounts per day");
        evidence.add("");

        // Step 1: Get total settled batch amounts for the month from Settled Batches report
        driver.get(ConfigReader.get("base.url") + "#/reports");
        reportsHub = new ReportsHubPage(driver);
        SettledBatchesPage settledBatches = reportsHub.navigateToSettledBatches();
        settledBatches.filterByMonth(TEST_MONTH, TEST_YEAR);

        long settledBatchesSum = getSettledBatchesAmountSum();
        String settledBatchesDisplay = formatCents(settledBatchesSum);
        LOG.info("Settled Batches Sum (Month {}): {} cents (${})", TEST_MONTH, settledBatchesSum, settledBatchesDisplay);
        evidence.add("Settled Batches Sum: $" + settledBatchesDisplay);

        // Step 2: Get sum of DAILY TOTAL funded amounts from Daily Discount Calculator
        driver.get(ConfigReader.get("base.url") + "#/reports");
        reportsHub = new ReportsHubPage(driver);
        DailyDiscountPage dailyDiscount = reportsHub.navigateToDailyDiscount();
        dailyDiscount.filterByMonth(TEST_MONTH, TEST_YEAR);

        long dailyDiscountSum = getDailyDiscountTotalSum();
        String dailyDiscountDisplay = formatCents(dailyDiscountSum);
        LOG.info("Daily Discount DAILY TOTAL Sum (Month {}): {} cents (${})", TEST_MONTH, dailyDiscountSum, dailyDiscountDisplay);
        evidence.add("Daily Discount DAILY TOTAL Sum: $" + dailyDiscountDisplay);

        // Step 3: Compare
        long difference = Math.abs(settledBatchesSum - dailyDiscountSum);
        evidence.add("Difference: $" + formatCents(difference));

        // Log evidence
        for (String line : evidence) {
            LOG.info("EVIDENCE: {}", line);
        }

        // Assertion: When working correctly, settled batches sum = daily discount daily totals sum
        // When BUG-07 is ON, daily totals are WRONG (only last batch per day)
        Assert.assertEquals(dailyDiscountSum, settledBatchesSum,
                String.format("Reconciliation R4: Σ settled batch amounts ($%s) should equal Σ Daily Discount DAILY TOTALS ($%s), " +
                        "but difference is $%s",
                        settledBatchesDisplay, dailyDiscountDisplay, formatCents(difference)));
    }

    /**
     * Get the sum of "Batch Amount" column from Settled Batches report.
     * This is the ground truth total for the month.
     */
    private long getSettledBatchesAmountSum() {
        try {
            // Find all batch rows (data-testid starts with "report-table-row-")
            List<WebElement> rows = driver.findElements(
                    By.cssSelector("[data-testid^='report-table-row-']"));

            long total = 0;
            for (WebElement row : rows) {
                try {
                    // The "Batch Amount" column is the 5th column (index 4)
                    // Columns: Batch Close Date(0), Close Time(1), Batch ID(2), Device ID(3),
                    //          Batch Amount(4), Trans Count(5), Deposit Date(6)
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    if (cells.size() >= 5) {
                        String amountText = cells.get(4).getText().trim();
                        if (!amountText.isEmpty() && amountText.contains("$")) {
                            long cents = parseMoneyToCents(amountText);
                            total += cents;
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Could not parse amount from row: {}", e.getMessage());
                }
            }
            return total;
        } catch (Exception e) {
            LOG.error("Error getting settled batches amount sum: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get the sum of "Funded Amount" column from Daily Discount Calculator report
     * but ONLY for rows with class 'daily-total' (the DAILY TOTAL summary rows).
     * These rows have number='DAILY TOTAL' and rowClass='daily-total'.
     */
    private long getDailyDiscountTotalSum() {
        try {
            // Find all rows with class 'daily-total' (the DAILY TOTAL summary rows)
            List<WebElement> dailyTotalRows = driver.findElements(
                    By.cssSelector("tr.daily-total"));

            long total = 0;
            for (WebElement row : dailyTotalRows) {
                try {
                    // The "Funded Amount" column is the 6th column (index 5)
                    // Columns: Batch Number(0), Batch Date(1), Number Of Items(2),
                    //          Discount Amount(3), Batch Amount(4), Funded Amount(5)
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    if (cells.size() >= 6) {
                        String amountText = cells.get(5).getText().trim();
                        if (!amountText.isEmpty() && amountText.contains("$")) {
                            long cents = parseMoneyToCents(amountText);
                            total += cents;
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Could not parse amount from daily total row: {}", e.getMessage());
                }
            }
            return total;
        } catch (Exception e) {
            LOG.error("Error getting daily discount total sum: " + e.getMessage());
            return 0;
        }
    }

    /** Parse money string like "$1,234.56" to cents as long */
    private long parseMoneyToCents(String money) {
        String cleaned = money.replace("$", "").replace(",", "").trim();
        return (long) (Double.parseDouble(cleaned) * 100);
    }

    /** Format cents back to dollars */
    private String formatCents(long cents) {
        return String.format("$%.2f", cents / 100.0);
    }
}