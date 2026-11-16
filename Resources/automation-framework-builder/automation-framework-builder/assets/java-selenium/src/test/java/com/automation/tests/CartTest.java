package com.automation.tests;

import com.automation.pages.CartPage;
import com.automation.pages.LoginPage;
import com.automation.pages.ProductsPage;
import com.automation.utils.DataProviders;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class CartTest extends BaseTest {

    @Test(dataProvider = "cartData", dataProviderClass = DataProviders.class, groups = {"smoke", "regression"},
          description = "Login, add a product to the cart and verify the cart")
    public void addToCartTest(Map<String, String> data) {
        ProductsPage products = new LoginPage(driver).loginAs(data.get("username"), data.get("password"));

        products.addToCart(data.get("productId"));
        int expected = Integer.parseInt(data.get("expectedCartCount"));
        Assert.assertEquals(products.getCartCount(), expected, "Cart badge count");

        CartPage cart = products.openCart();
        List<String> items = cart.getItemNames();
        Assert.assertEquals(items.size(), expected, "Items listed in cart: " + items);
    }
}
