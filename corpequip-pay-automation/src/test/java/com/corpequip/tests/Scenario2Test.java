package com.corpequip.tests;

import com.corpequip.base.DriverFactory;
import com.corpequip.pages.Dashboard;
import com.corpequip.pages.ReportsHubPage;
import com.corpequip.pages.TotalsSummaryPage;
import com.corpequip.listeners.RetryAnalyzer;
import com.corpequip.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario 2: Net vs Gross Comparison (BUG-01)
 *
 * Bug Description:
 * - BUG-01 causes the Totals Summary report to show gross sales instead of net
 * (sales - refunds)
 * - Business Overview batch total shows the correct net amount
 *
 * Test Approach:
 * 1. Navigate to Dashboard → Select "2026" in Business Overview
 * 2. Read Batch Total from Business Overview (year total - net)
 * 3. Navigate to Totals Summary Report → Loop through each month (Jan-Nov)
 * 4. Sum the monthly totals from Totals Summary
 * 5. Compare both values - the sum will be GREATER than the year total when
 * BUG-01 is ON
 *
 * Expected Result when BUG-01 is ON:
 * - Totals Summary monthly sum > Business Overview year total
 * - Difference equals total refunds (refunds are included in gross but deducted
 * from net)
 *
 * The test runs against the bug being ACTIVE and will FAIL when the bug is
 * fixed.
 */
public class Scenario2Test extends BaseTest {

    private static final Logger LOG = LogManager.getLogger(Scenario2Test.class);
    private static final String TEST_YEAR = "2026";

    /** Month numbers for 2026 (Jan=1, Feb=2, ..., Nov=11) */
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
        // Use JavaScript to navigate directly to dashboard (the app uses hash routing)
        driver.get(ConfigReader.get("base.url") + "#/dashboard");
    }

    @Override
    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    @Test(description = "Scenario 2: Net vs Gross comparison - verification", retryAnalyzer = RetryAnalyzer.class, groups = {
            "smoke", "regression" })
    public void scenario2_netVsGrossComparison() {
        LOG.info("=== Scenario 2: Net vs Gross Comparison  ===");
        LOG.info("Testing Year: {}", TEST_YEAR);

        // Step 1: Navigate to Dashboard and get Business Overview Batch Total (yearly
        // total)
        Dashboard dashboard = new Dashboard(driver);

        // Step 2: Select year 2026 for Business Overview
        dashboard.selectOverviewYear(TEST_YEAR);
        String overviewLabel = dashboard.getOverviewLabel();
        LOG.info("Overview period label: {}", overviewLabel);
        Assert.assertTrue(overviewLabel.contains(TEST_YEAR),
                "Overview should show data for " + TEST_YEAR);

        // Step 3: Get Batch Total from Business Overview (this is the correct net
        // amount)
        String batchTotalDisplay = dashboard.getBatchTotal();
        long batchTotalCents = parseMoneyToCents(batchTotalDisplay);
        LOG.info("Business Overview Batch Total (Year): {} cents (${})", batchTotalCents, batchTotalDisplay);

        // Step 4: Navigate to Totals Summary Report
        ReportsHubPage reportsHub = dashboard.navigateToReports();
        TotalsSummaryPage totalsSummary = reportsHub.navigateToTotalsSummary();

        // Step 5: Loop through each month (Jan-Nov) and sum the monthly totals
        long monthlySumCents = 0L;
        List<String> monthlyEvidence = new ArrayList<>();

        LOG.info("Looping through months 1-11 to sum Totals Summary totals...");

        for (String monthNum : MONTHS_2026) {
            try {
                // Filter by month
                totalsSummary.filterByMonth(monthNum, TEST_YEAR);
                String reportPeriod = totalsSummary.getReportPeriod();

                // Get monthly total
                String monthlyTotalDisplay = totalsSummary.getTotalSubmittedAmount();
                long monthlyCents = totalsSummary.getTotalSubmittedInCents();

                LOG.info("Month {} - Report Period: {}, Monthly Total: {} cents (${})",
                        monthNum, reportPeriod, monthlyCents, monthlyTotalDisplay);

                monthlySumCents += monthlyCents;
                monthlyEvidence.add(String.format("Month %s: %s cents (%s)",
                        monthNum, formatCents(monthlyCents), monthlyTotalDisplay));
            } catch (Exception e) {
                LOG.error("Failed to process month " + monthNum + ": " + e.getMessage());
                // Continue with next month on error
            }
        }

        LOG.info("Sum of monthly totals: {} cents (${})", monthlySumCents, formatCents(monthlySumCents));

        // Step 6: Store values for evidence
        List<String> evidence = new ArrayList<>();
        evidence.add("Test Year: " + TEST_YEAR);
        evidence.add("Business Overview Batch Total (Year): $" + formatCents(batchTotalCents));
        evidence.add("Totals Summary Monthly Sum: $" + formatCents(monthlySumCents));

        // Log monthly breakdown
        for (String line : monthlyEvidence) {
            LOG.info("EVIDENCE: {}", line);
        }

        // Step 7: Calculate difference
        long differenceCents = Math.abs(monthlySumCents - batchTotalCents);
        evidence.add("Difference: $" + formatCents(differenceCents));

        // Step 8: Verification logic
        // When BUG-01 is ON: monthly sum will be GREATER than batch total (gross > net)
        // The difference represents refunds that are included in gross but deducted
        // from net
        if (monthlySumCents > batchTotalCents) {
            evidence.add("VERIFIED:  is ACTIVE - Totals Summary shows gross sales");
            evidence.add("Difference ($" + formatCents(differenceCents) + ") = Total refunds not deducted");
            LOG.info("VERIFIED: is ACTIVE - Totals Summary gross > Business Overview net");
            LOG.info("Difference (${}): Total refunds included but not deducted", formatCents(differenceCents));
        } else {
            evidence.add("UNEXPECTED:  Values match unexpectedly");
            LOG.warn("UNEXPECTED: Totals Summary does not exceed Business Overview - bug may be fixed");
        }

        // Print evidence for the report
        for (String line : evidence) {
            LOG.info("EVIDENCE: {}", line);
        }

        // Assertion: Monthly sum should EQUAL year total (correct behavior)
        // When BUG-01 is ON: monthly sum > year total → TEST FAILS
        // When BUG-01 is OFF: monthly sum = year total → TEST PASSES
        Assert.assertEquals(monthlySumCents, batchTotalCents,
                String.format(
                        "Totals Summary monthly sum ($%s) should equal Business Overview year total ($%s), but differs by $%s",
                        formatCents(monthlySumCents), formatCents(batchTotalCents), formatCents(differenceCents)));
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