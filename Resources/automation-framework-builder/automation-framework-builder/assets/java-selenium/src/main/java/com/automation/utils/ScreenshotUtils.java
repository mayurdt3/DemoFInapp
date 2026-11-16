package com.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ScreenshotUtils {

    private static final Path DIR = Paths.get("output", "screenshots");

    private ScreenshotUtils() { }

    /** Saves a PNG into output/screenshots and returns its path. */
    public static Path capture(WebDriver driver, String name) {
        try {
            Files.createDirectories(DIR);
            String safe = name.replaceAll("[^a-zA-Z0-9_-]+", "_");
            Path target = DIR.resolve(safe + "_" + System.currentTimeMillis() + ".png");
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(target, png);
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Could not save screenshot", e);
        }
    }

    public static String asBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }
}
