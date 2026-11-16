package com.corpequip.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Retries failed tests up to a configurable count. Used for flaky tests. */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;
    private final int maxRetryCount;

    public RetryAnalyzer() {
        this.maxRetryCount = Integer.parseInt(System.getProperty("retry.count", "1"));
    }

    public RetryAnalyzer(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            LOG.warn("Retrying {} (attempt {}/{})", result.getMethod().getMethodName(), retryCount, maxRetryCount);
            return true;
        }
        return false;
    }

    public int getRetryCount() {
        return retryCount;
    }
}