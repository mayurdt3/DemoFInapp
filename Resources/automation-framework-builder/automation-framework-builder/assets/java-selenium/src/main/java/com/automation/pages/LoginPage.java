package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    // Locators are private (encapsulation): tests call behavior, never touch elements.
    private final By usernameInput = By.cssSelector("[data-test='username']");
    private final By passwordInput = By.cssSelector("[data-test='password']");
    private final By loginButton = By.cssSelector("[data-test='login-button']");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        click(loginButton);
    }

    /** Successful login lands on the products page. */
    public ProductsPage loginAs(String username, String password) {
        login(username, password);
        return new ProductsPage(driver);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}
