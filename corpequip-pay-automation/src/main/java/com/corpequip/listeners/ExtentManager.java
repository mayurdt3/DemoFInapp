package com.corpequip.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.corpequip.utils.ConfigReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Owns the single ExtentReports instance and a per-thread ExtentTest. */
public final class ExtentManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    private ExtentManager() { }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            try {
                Files.createDirectories(Paths.get("output", "reports"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            ExtentSparkReporter spark = new ExtentSparkReporter("output/reports/ExtentReport.html");
            spark.config().setDocumentTitle("Automation Test Report");
            spark.config().setReportName("CorpEquip Pay Automation");
            spark.config().setTheme(Theme.STANDARD);
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", ConfigReader.get("env"));
            extent.setSystemInfo("Browser", ConfigReader.get("browser"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
        }
        return extent;
    }

    public static ExtentTest startTest(String name) {
        ExtentTest test = getInstance().createTest(name);
        CURRENT.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return CURRENT.get();
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}