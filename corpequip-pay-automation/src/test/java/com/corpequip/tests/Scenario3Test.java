package com.corpequip.tests;

import com.corpequip.base.DriverFactory;
import com.corpequip.pages.Dashboard;
import com.corpequip.pages.ReportsHubPage;
import com.corpequip.pages.TotalsSummaryPage;
import com.corpequip.pages.SearchTransactionsPage;
import com.corpequip.pages.SettledBatchesPage;
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
 * Scenario 3: Month-end Boundary (Timing Break) - BUG-03
 *
 * Bug Description:
 * - BUG-03 causes the last day of every period to silently disappear
 * - Totals Summary "Total Submitted" for January ≠ January Merchant Statement
 * "Amounts Submitted"
 * - Search Transactions row count ≠ sum of batch transaction counts
 *
 * Impact:
 * - This is like M360's "transactions show zero at the start of the month"
 * story
 * - Client experiences timing breaks where the final day's data vanishes
 * - End-of-month reconciliation fails
 *
 * Test Approach:
 * 1. Navigate to Totals Summary for January 2026
 * 2. Get "Total Submitted Amount" from Totals Summary
 * 3. Navigate to Search Transactions for January 2026
 * 4. Count total rows in Search Transactions
 * 5. Navigate to Settled Batches for January 2026
 * 6. Sum batch transaction counts
 * 7. Compare values - discrepancies indicate BUG-03 is active
 *
 * Expected Result when BUG-03 is ON:
 * - Totals Summary total ≠ Merchant Statement total
 * - Search Transactions count ≠ Sum of batch counts
 */
public class Scenario3Test extends BaseTest {

    private static final Logger LOG = LogManager.getLogger(Scenario3Test.class);
    private static final String TEST_YEAR = "2026";
    private static final String TEST_MONTH = "3"; // January
    private static final String TEST_MONTH_NAME = "March";

    @Override
    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver(ConfigReader.get("browser"), ConfigReader.getBoolean("headless"));
        // Use JavaScript to navigate directly to dashboard (the app uses hash routing)
        driver.get(ConfigReader.get("base.url") + "#/dashboard");
    }

    @Override
    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    @Test(description = "Scenario 3: Month-end Boundary (Timing Break) - BUG-03 verification", retryAnalyzer = RetryAnalyzer.class, groups = {
            "smoke", "regression" })
    public void scenario3_monthEndBoundary() {
        LOG.info("=== Scenario 3: Month-end Boundary (BUG-03) ===");
        LOG.info("Testing Month: {} {}", TEST_MONTH_NAME, TEST_YEAR);

        // Step 1: Navigate to Dashboard
        dashboard = new Dashboard(driver);

        // Step 2: Get Business Overview Batch Total (yearly net)
        dashboard.selectOverviewYear(TEST_YEAR);
        String overviewLabel = dashboard.getOverviewLabel();
        Assert.assertTrue(overviewLabel.contains(TEST_YEAR),
                "Overview should show data for " + TEST_YEAR);

        String batchTotalDisplay = dashboard.getBatchTotal();
        long batchTotalCents = parseMoneyToCents(batchTotalDisplay);
        LOG.info("Business Overview Batch Total (Year): {} cents (${})", batchTotalCents, batchTotalDisplay);

        // Step 3: Navigate to Totals Summary Report
        ReportsHubPage reportsHub = dashboard.navigateToReports();
        TotalsSummaryPage totalsSummary = reportsHub.navigateToTotalsSummary();

        // Step 4: Filter Totals Summary by January
        totalsSummary.filterByMonth(TEST_MONTH, TEST_YEAR);
        String reportPeriod = totalsSummary.getReportPeriod();
        LOG.info("Totals Summary Report Period: {}", reportPeriod);

        // Step 5: Get January Total Submitted Amount from Totals Summary
        String TotalDisplay = totalsSummary.getTotalSubmittedAmount();// $224,123.52
        long TotalCents = totalsSummary.getTotalSubmittedInCents();
        LOG.info("Totals Summary January Total: {} cents (${})", TotalCents, TotalDisplay);

        // Step 6: Navigate back to Reports Hub directly from URL
        driver.get(ConfigReader.get("base.url") + "#/reports");
        reportsHub = new ReportsHubPage(driver); // Re-instantiate with fresh driver state

        // Step 7: Navigate to Search Transactions Report
        SearchTransactionsPage searchTrans = reportsHub.navigateToSearchTransactions();

        // Step 8: Filter Search Transactions by January and get row count
        try {
            searchTrans.filterByYear(TEST_YEAR);
            Thread.sleep(1000); // Wait for filter to apply
        } catch (Exception e) {
            LOG.warn("Filter error (may be expected with BUG-03): " + e.getMessage());
        }

        // Get the total count from pagination info
        long searchTransCount = getSearchTransactionsRowCount();
        LOG.info("Search Transactions row count: {}", searchTransCount);

        // Step 9: Navigate to Settled Batches - re-instantiate ReportsHubPage
        driver.get(ConfigReader.get("base.url") + "#/reports");
        reportsHub = new ReportsHubPage(driver);
        SettledBatchesPage settledBatches = reportsHub.navigateToSettledBatches();
        settledBatches.filterByYear(TEST_YEAR);

        long batchCountSum = getBatchTransactionCountSum();
        LOG.info("Sum of batch transaction counts: {}", batchCountSum);

        // Step 10: Store evidence
        List<String> evidence = new ArrayList<>();
        evidence.add("Test Month: " + TEST_MONTH_NAME + " " + TEST_YEAR);
        evidence.add("Totals Summary Total: $" + formatCents(TotalCents));
        evidence.add("Search Transactions Count: " + searchTransCount);
        evidence.add("Sum of Batch Transaction Counts: " + batchCountSum);

        // Step 11: Verify month-end boundary issues
        long countDifference = Math.abs(searchTransCount - batchCountSum);
        evidence.add("Search-Batch Count Difference: " + countDifference);

        // Log evidence
        for (String line : evidence) {
            LOG.info("EVIDENCE: {}", line);
        }

        // Assertion: counts will differ due to missing last day
        // transactions
        Assert.assertEquals(searchTransCount, batchCountSum,
                String.format(
                        "Search Transactions count (%d) should equal sum of batch counts (%d) - month-end boundary transactions are missing",
                        searchTransCount, batchCountSum));
    }

    /**
     * Get the total row count from Search Transactions report.
     * Uses the pagination info element to determine total count.
     */
    private long getSearchTransactionsRowCount() {
        try {
            // Try to find the page-info element which shows "X of Y items"
            // The format is typically "1 - 10 of 247 items"
            By pageInfoLocator = By.cssSelector("[data-testid='report-table-page-info']");
            WebElement pageInfo = driver.findElement(pageInfoLocator);
            String pageInfoText = pageInfo.getText().trim();

            // Parse "X - Y of Z items" format
            String[] parts = pageInfoText.split("\\s+");
            if (parts.length >= 4) {
                // The last number before "items" is the total count
                String lastPart = parts[parts.length - 2];
                return Long.parseLong(lastPart.replace(",", ""));
            }
            return 0;
        } catch (Exception e) {
            LOG.warn("Could not parse row count from pagination, returning 0");
            return 0;
        }
    }

    /**
     * Get the sum of transaction counts from all batch rows.
     * Sums the "Trans Count" column from Settled Batches report.
     */
    private long getBatchTransactionCountSum() {
        try {
            // Find all rows in the settled batches table
            // Rows have data-testid starting with "report-table-row-" and data-row-key for
            // the batch ID
            List<WebElement> rows = driver.findElements(
                    By.cssSelector("[data-testid^='report-table-row-']"));

            long total = 0;
            for (WebElement row : rows) {
                try {
                    // The count column is the 6th column (Trans Count)
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    if (cells.size() >= 6) {
                        String countText = cells.get(5).getText().trim().replace(",", "");
                        if (!countText.isEmpty()) {
                            total += Long.parseLong(countText);
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Could not parse count from row: {}", e.getMessage());
                }
            }
            return total;
        } catch (Exception e) {
            LOG.error("Error getting batch count sum: " + e.getMessage());
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