package com.corpequip.tests;

import com.corpequip.base.DriverFactory;
import com.corpequip.pages.Dashboard;
import com.corpequip.pages.SearchTransactionsPage;
import com.corpequip.pages.SettledBatchesPage;
import com.corpequip.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Base test class providing setup/teardown for all UI tests.
 * Each test method gets a fresh browser instance (thread-safe through
 * DriverFactory).
 */
public abstract class BaseTest {

    protected static final Logger LOG = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    SettledBatchesPage settledBatches;
    SearchTransactionsPage searchTrans;
    Dashboard dashboard;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver(ConfigReader.get("browser"), ConfigReader.getBoolean("headless"));
        driver.get(ConfigReader.get("base.url"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}