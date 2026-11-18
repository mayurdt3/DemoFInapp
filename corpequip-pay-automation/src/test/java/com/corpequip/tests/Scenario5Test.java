package com.corpequip.tests;

import com.corpequip.base.DriverFactory;
import com.corpequip.pages.ReportsHubPage;
import com.corpequip.pages.SettledBatchesPage;
import com.corpequip.pages.AchDepositPage;
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
 * Scenario 5: ACH Deposit Reconciliation - Reconciliation R2
 *
 * Business Rule (from Reconciliation R2):
 * "Σ settled batch amounts = Σ ACH batch deposits (BTOT)"
 *
 * This validates that every settled batch amount is deposited to the bank
 * as an ACH deposit with CRD Type = 'BTOT' (Batch Total).
 *
 * When working correctly:
 * - Sum of all Settled Batches "Batch Amount" = Sum of ACH Deposits with tranCode "Checking Deposit" and CRD Type "BTOT"
 *
 * Test Approach:
 * 1. Navigate to Settled Batches for full year 2026 → sum "Batch Amount" column
 * 2. Navigate to ACH Deposit Report → filter by tranCode "Checking Deposit" → sum "Amount" column
 * 3. Compare both values
 *
 * Expected when BUG-02 is OFF: Both sums should match exactly
 * When BUG-02 is ON: Date range filter excludes end date → potential mismatch
 */
public class Scenario5Test extends BaseTest {

    private static final Logger LOG = LogManager.getLogger(Scenario5Test.class);
    private static final String TEST_YEAR = "2026";

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

    @Test(description = "Scenario 5: ACH Deposit Reconciliation - Reconciliation R2",
          retryAnalyzer = RetryAnalyzer.class,
          groups = {"smoke", "regression"})
    public void scenario5_achDepositReconciliation() {
        LOG.info("=== Scenario 5: ACH Deposit Reconciliation (Reconciliation R2) ===");
        LOG.info("Testing Year: {}", TEST_YEAR);

        // Navigate to Dashboard
        com.corpequip.pages.Dashboard dashboard = new com.corpequip.pages.Dashboard(driver);
        ReportsHubPage reportsHub = dashboard.navigateToReports();

        // Evidence collection
        List<String> evidence = new ArrayList<>();
        evidence.add("Test Year: " + TEST_YEAR);
        evidence.add("Reconciliation: R2 - Σ settled batch amounts = Σ ACH batch deposits (BTOT)");
        evidence.add("");

        // Step 1: Get total settled batch amounts for full year 2026
        driver.get(ConfigReader.get("base.url") + "#/reports");
        reportsHub = new ReportsHubPage(driver);
        SettledBatchesPage settledBatches = reportsHub.navigateToSettledBatches();
        settledBatches.filterByYear(TEST_YEAR);

        long settledBatchesSum = getSettledBatchesAmountSum();
        String settledBatchesDisplay = formatCents(settledBatchesSum);
        LOG.info("Settled Batches Sum (Year {}): {} cents (${})", TEST_YEAR, settledBatchesSum, settledBatchesDisplay);
        evidence.add("Settled Batches Sum: $" + settledBatchesDisplay);

        // Step 2: Get total ACH batch deposits (Checking Deposit, CRD Type = BTOT)
        driver.get(ConfigReader.get("base.url") + "#/reports");
        reportsHub = new ReportsHubPage(driver);
        AchDepositPage achDeposit = reportsHub.navigateToAchDeposit();

        // Filter by tranCode "Checking Deposit" and year 2026
        achDeposit.filterByTranCodeAndYear("Checking Deposit", TEST_YEAR);
        long achDepositsSum = getAchDepositsAmountSum();
        String achDepositsDisplay = formatCents(achDepositsSum);
        LOG.info("ACH Deposits Sum (Checking Deposit, Year {}): {} cents (${})", TEST_YEAR, achDepositsSum, achDepositsDisplay);
        evidence.add("ACH Deposits Sum (Checking Deposit): $" + achDepositsDisplay);

        // Step 3: Compare
        long difference = Math.abs(settledBatchesSum - achDepositsSum);
        evidence.add("Difference: $" + formatCents(difference));

        // Log evidence
        for (String line : evidence) {
            LOG.info("EVIDENCE: {}", line);
        }

        // Assertion: When working correctly, settled batches sum = ACH deposits sum
        Assert.assertEquals(achDepositsSum, settledBatchesSum,
                String.format("Reconciliation R2: Σ settled batch amounts ($%s) should equal Σ ACH batch deposits (BTOT) ($%s), " +
                        "but difference is $%s",
                        settledBatchesDisplay, achDepositsDisplay, formatCents(difference)));
    }

    /**
     * Get the sum of "Batch Amount" column from Settled Batches report.
     */
    private long getSettledBatchesAmountSum() {
        try {
            List<WebElement> rows = driver.findElements(
                    By.cssSelector("[data-testid^='report-table-row-']"));

            long total = 0;
            for (WebElement row : rows) {
                try {
                    // Batch Amount is 5th column (index 4)
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    if (cells.size() >= 5) {
                        String amountText = cells.get(4).getText().trim();
                        if (!amountText.isEmpty() && amountText.contains("$")) {
                            total += parseMoneyToCents(amountText);
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
     * Get the sum of "Amount" column from ACH Deposit report (filtered to Checking Deposit).
     */
    private long getAchDepositsAmountSum() {
        try {
            List<WebElement> rows = driver.findElements(
                    By.cssSelector("[data-testid^='report-table-row-']"));

            long total = 0;
            for (WebElement row : rows) {
                try {
                    // Amount is 2nd column (index 1) in ACH Deposit report
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    if (cells.size() >= 2) {
                        String amountText = cells.get(1).getText().trim();
                        if (!amountText.isEmpty() && amountText.contains("$")) {
                            total += parseMoneyToCents(amountText);
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Could not parse amount from ACH row: {}", e.getMessage());
                }
            }
            return total;
        } catch (Exception e) {
            LOG.error("Error getting ACH deposits amount sum: " + e.getMessage());
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