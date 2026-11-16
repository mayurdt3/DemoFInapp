package com.automation.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

/** Creates browsers and keeps one WebDriver per thread so tests can run in parallel. Selenium Manager downloads drivers. */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() { }

    public static WebDriver createDriver(String browser, boolean headless) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "firefox":
                FirefoxOptions firefox = new FirefoxOptions();
                if (headless) {
                    firefox.addArguments("-headless");
                }
                driver = new FirefoxDriver(firefox);
                break;
            case "edge":
                EdgeOptions edge = new EdgeOptions();
                if (headless) {
                    edge.addArguments("--headless=new");
                }
                driver = new EdgeDriver(edge);
                break;
            case "chrome":
            default:
                ChromeOptions chrome = new ChromeOptions();
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("credentials_enable_service", false);
                prefs.put("profile.password_manager_enabled", false);
                prefs.put("profile.password_manager_leak_detection", false);
                chrome.setExperimentalOption("prefs", prefs);
                chrome.addArguments("--window-size=1920,1080", "--disable-notifications");
                if (headless) {
                    chrome.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                }
                driver = new ChromeDriver(chrome);
        }
        DRIVER.set(driver);
        return driver;
    }

    /** Returns this thread's driver, or null when the current test does not use a browser (e.g. API tests). */
    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
