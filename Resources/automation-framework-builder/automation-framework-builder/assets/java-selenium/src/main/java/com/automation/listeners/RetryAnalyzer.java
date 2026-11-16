package com.automation.listeners;

import com.automation.utils.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Re-runs a failed test up to retry.count times (override with -Dretry.count=2). */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempts = 0;
    private final int maxRetries = ConfigReader.getInt("retry.count");

    @Override
    public boolean retry(ITestResult result) {
        if (attempts < maxRetries) {
            attempts++;
            return true;
        }
        return false;
    }
}
