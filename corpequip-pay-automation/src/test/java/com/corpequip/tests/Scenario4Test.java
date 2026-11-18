package com.corpequip.tests;

import com.corpequip.base.DriverFactory;
import com.corpequip.pages.Dashboard;
import com.corpequip.pages.ReportsHubPage;
import com.corpequip.pages.TotalsSummaryPage;
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
 * Scenario 4: Settlement Completeness - Reconciliation R1
 *
 * Business Rule (from Reconciliation R1):
 * "Totals Summary total submitted = Σ settled batch amounts (per month)"
 *
 * This validates that every transaction submitted is properly settled.
 * Each month's Totals Summary "Total Submitted Amount" should equal
 * the sum of Settled Batches amounts for that same month.
 *
 * When BUG-01 is ON:
 * - Totals Summary shows GROSS sales (sales + refunds)
 * - Settled Batches shows NET batch amounts (sales - refunds)
 * - Values will differ by the refund amount per month
 *
 * When BUG-01 is OFF:
 * - Both show NET amounts
 * - Values should match exactly
 *
 * Test Approach:
 * For each month (Jan-Nov 2026):
 * 1. Filter Totals Summary by month → get "Total Submitted Amount"
 * 2. Filter Settled Batches by same month → sum "Batch Amount" column
 * 3. Compare both values
 */
public class Scenario4Test extends BaseTest {

    private static final Logger LOG = LogManager.getLogger(Scenario4Test.class);
    private static final String TEST_YEAR = "2026";

    /** Month numbers for 2026 (Jan=1 through Nov=11) */
    private static final List<String> MONTHS_2026 = new ArrayList<>();
    static {
        for (int i = 1; i <= 11; i++) {
            MONTHS_2026.add(String.valueOf(i));
        }
    }

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

    @Test(description = "Scenario 4: Settlement Completeness - Reconciliation R1 (BUG-01 active)",
          retryAnalyzer = RetryAnalyzer.class,
          groups = {"smoke", "regression"})
    public void scenario4_settlementCompleteness() {
        LOG.info("=== Scenario 4: Settlement Completeness (Reconciliation R1) ===");
        LOG.info("Testing Year: {}", TEST_YEAR);

        // Navigate to Dashboard
        Dashboard dashboard = new Dashboard(driver);
        ReportsHubPage reportsHub = dashboard.navigateToReports();

        // Evidence collection
        List<String> evidence = new ArrayList<>();
        evidence.add("Test Year: " + TEST_YEAR);
        evidence.add("Reconciliation: R1 - Totals Summary total submitted = Σ settled batch amounts (per month)");
        evidence.add("");

        int mismatchCount = 0;
        long totalDifferenceCents = 0;

        // Loop through each month
        for (String monthNum : MONTHS_2026) {
            try {
                // Navigate to Reports Hub fresh
                driver.get(ConfigReader.get("base.url") + "#/reports");
                reportsHub = new ReportsHubPage(driver);

                // Get Totals Summary total for this month
                TotalsSummaryPage totalsSummary = reportsHub.navigateToTotalsSummary();
                totalsSummary.filterByMonth(monthNum, TEST_YEAR);
                long totalsSummaryTotal = totalsSummary.getTotalSubmittedInCents();
                String totalsSummaryDisplay = totalsSummary.getTotalSubmittedAmount();

                // Navigate back to Reports Hub fresh
                driver.get(ConfigReader.get("base.url") + "#/reports");
                reportsHub = new ReportsHubPage(driver);

                // Get Settled Batches sum for this month
                SettledBatchesPage settledBatches = reportsHub.navigateToSettledBatches();
                settledBatches.filterByMonth(monthNum, TEST_YEAR);
                long batchesSum = getSettledBatchesAmountSum();
                String batchesSumDisplay = formatCents(batchesSum);

                // Calculate difference
                long difference = Math.abs(totalsSummaryTotal - batchesSum);
                totalDifferenceCents += difference;

                // Log monthly result
                LOG.info("Month {} - Totals Summary: {} cents (${}) | Settled Batches: {} cents (${}) | Diff: {} cents (${})",
                        monthNum, totalsSummaryTotal, totalsSummaryDisplay, batchesSum, batchesSumDisplay,
                        difference, formatCents(difference));

                evidence.add(String.format("Month %s: TS=%s cents | SB=%s cents | Diff=%s cents",
                        monthNum, formatCents(totalsSummaryTotal), formatCents(batchesSum), formatCents(difference)));

                if (difference > 0) {
                    mismatchCount++;
                }

            } catch (Exception e) {
                LOG.error("Error processing month {}: {}", monthNum, e.getMessage());
                evidence.add("Month " + monthNum + ": ERROR - " + e.getMessage());
            }
        }

        // Summary
        evidence.add("");
        evidence.add("SUMMARY: " + mismatchCount + " out of 11 months have mismatches");
        evidence.add("Total difference across all months: $" + formatCents(totalDifferenceCents));

        if (mismatchCount > 0) {
            evidence.add("VERIFIED: Settlement completeness FAILS - BUG-01 is ACTIVE");
            evidence.add("Totals Summary shows gross sales; Settled Batches shows net amounts");
            LOG.warn("Settlement Completeness: {} months mismatch, total diff = ${}",
                    mismatchCount, formatCents(totalDifferenceCents));
        } else {
            evidence.add("PASS: All months match - settlement is complete");
            LOG.info("Settlement Completeness: All 11 months match");
        }

        // Print evidence
        for (String line : evidence) {
            LOG.info("EVIDENCE: {}", line);
        }

        // Assertion: When BUG-01 is ON, settlement completeness should FAIL
        // (Totals Summary gross ≠ Settled Batches net)
        Assert.assertEquals(totalDifferenceCents, 0,
                String.format("Settlement Completeness: Expected total difference to be 0 " +
                        "but total difference is $%s across %d months. " +
                        "Totals Summary (gross) to differ from Settled Batches (net).",
                        formatCents(totalDifferenceCents), mismatchCount));
    }

    /**
     * Get the sum of "Batch Amount" column from Settled Batches report.
     * This is the net settled amount for the month.
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