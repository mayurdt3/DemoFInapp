package com.corpequip.base;

import com.corpequip.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract parent of all page classes.
 * Uses PageFactory with @FindBy annotations for element initialization.
 * Provides explicit waits and logged, reusable actions. No Thread.sleep
 * anywhere.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds")));
        // Initialize all @FindBy annotated fields
        PageFactory.initElements(driver, this);
    }

    /** Click an element by locator */
    protected void click(By locator) {
        log.info("Click {}", locator);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Click a WebElement */
    protected void click(WebElement element) {
        log.info("Click element {}", element);
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    /** Type text into an element by locator */
    protected void type(By locator, String text) {
        log.info("Type '{}' into {}", text, locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    /** Get text from an element by locator */
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    /** Get text from a WebElement */
    protected String getText(WebElement element) {
        return element.getText().trim();
    }

    /** Get texts from multiple elements by locator */
    protected List<String> getTexts(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return driver.findElements(locator).stream().map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    /** Check if element is displayed by locator */
    protected boolean isDisplayed(By locator) {
        return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
    }

    /** Select option by visible text from a dropdown (By locator) */
    protected void selectByText(By locator, String text) {
        log.info("Select '{}' from {}", text, locator);
        org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)));
        dropdown.selectByVisibleText(text);
    }

    /** Select option by visible text from a dropdown (WebElement) */
    protected void selectByText(WebElement element, String text) {
        log.info("Select '{}' from element {}", text, element);
        Select dropdown = new Select(element);
        dropdown.selectByVisibleText(text);
    }

    /** Select option by visible text from a dropdown (WebElement) */
    protected void selectByValue(WebElement element, String text) {
        log.info("Select '{}' from element {}", text, element);
        Select dropdown = new Select(element);
        dropdown.selectByValue(text);
    }

    /** Get attribute value from element by locator */
    protected String getAttribute(By locator, String attribute) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getAttribute(attribute);
    }

    /** Get WebElement from @FindBy field by name */
    protected WebElement getField(String fieldName) {
        try {
            Field field = getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (WebElement) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException("Field not found: " + fieldName, e);
        }
    }

    /** Wait for element to be visible */
    protected void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Wait for element to be clickable */
    protected void waitForClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}