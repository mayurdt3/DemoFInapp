package com.automation.listeners;

import com.automation.base.DriverFactory;
import com.automation.utils.ExcelUtils;
import com.automation.utils.ScreenshotUtils;
import com.aventstack.extentreports.ExtentTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Connects TestNG to Extent Reports, logs, failure screenshots and the Excel results file. */
public class TestListener implements ITestListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);
    private static final List<List<Object>> RESULTS = Collections.synchronizedList(new ArrayList<>());

    private static String displayName(ITestResult result) {
        String name = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        if (params.length > 0 && params[0] instanceof Map) {
            name += " [" + ((Map<?, ?>) params[0]).get("testId") + "]";
        }
        return name;
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("START {}", displayName(result));
        ExtentTest test = ExtentManager.startTest(displayName(result));
        test.assignCategory(result.getMethod().getGroups());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS  {}", displayName(result));
        ExtentManager.getTest().pass("Test passed");
        record(result, "PASS", "");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL  {}", displayName(result), result.getThrowable());
        ExtentTest test = ExtentManager.getTest();
        test.fail(result.getThrowable());
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) { // API tests have no browser
            ScreenshotUtils.capture(driver, displayName(result));
            test.addScreenCaptureFromBase64String(ScreenshotUtils.asBase64(driver), "Failure screenshot");
        }
        record(result, "FAIL", String.valueOf(result.getThrowable().getMessage()).split("\n")[0]);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String status = result.wasRetried() ? "RETRIED" : "SKIP";
        LOG.warn("{}  {}", status, displayName(result));
        ExtentManager.getTest().skip(status);
        record(result, status, "");
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
        ExcelUtils.writeSheet("output/excel/TestResults.xlsx", "Results",
                Arrays.asList("Test", "Groups", "Status", "Duration(ms)", "Error"),
                new ArrayList<>(RESULTS));
    }

    private static void record(ITestResult result, String status, String error) {
        RESULTS.add(Arrays.asList(displayName(result), String.join(",", result.getMethod().getGroups()),
                status, result.getEndMillis() - result.getStartMillis(), error));
    }
}
