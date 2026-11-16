package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductsPage extends BasePage {

    private final By title = By.cssSelector("[data-test='title']");
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");
    private final By cartLink = By.cssSelector("[data-test='shopping-cart-link']");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public String getTitle() {
        return getText(title);
    }

    public void addToCart(String productId) {
        click(By.cssSelector("[data-test='add-to-cart-" + productId + "']"));
    }

    /** Returns 0 when the badge is not shown (empty cart). */
    public int getCartCount() {
        return isDisplayed(cartBadge) ? Integer.parseInt(getText(cartBadge)) : 0;
    }

    public CartPage openCart() {
        click(cartLink);
        return new CartPage(driver);
    }
}
