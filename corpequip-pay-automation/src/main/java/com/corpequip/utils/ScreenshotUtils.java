package com.corpequip.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Handles screenshot capture for failures and diagnostics. */
public final class ScreenshotUtils {

    private static final Logger LOG = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private ScreenshotUtils() { }

    /** Captures a screenshot as a Base64 string. */
    public static String asBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    /** Captures a screenshot to disk and returns the file path. */
    public static String capture(WebDriver driver, String testName) {
        try {
            String timestamp = LocalDateTime.now().format(DT_FORMAT);
            String fileName = testName + "_" + timestamp + ".png";
            Path dir = Paths.get("output", "screenshots");
            Files.createDirectories(dir);
            Path path = dir.resolve(fileName);
            // Use Java NIO to copy the screenshot
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(source.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Screenshot saved: {}", path);
            return path.toString();
        } catch (IOException e) {
            LOG.error("Failed to capture screenshot", e);
            return null;
        }
    }
}