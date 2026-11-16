package com.automation.tests;

import com.automation.pages.LoginPage;
import com.automation.pages.ProductsPage;
import com.automation.utils.DataProviders;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class LoginTest extends BaseTest {

    @Test(dataProvider = "loginData", dataProviderClass = DataProviders.class, groups = {"smoke", "regression"},
          description = "Login with valid and invalid credentials from Excel")
    public void loginTest(Map<String, String> data) {
        LoginPage loginPage = new LoginPage(driver);

        if ("SUCCESS".equals(data.get("expectedResult"))) {
            ProductsPage products = loginPage.loginAs(data.get("username"), data.get("password"));
            Assert.assertEquals(products.getTitle(), "Products", "Products page should open after login");
        } else {
            loginPage.login(data.get("username"), data.get("password"));
            Assert.assertTrue(loginPage.getErrorMessage().contains(data.get("expectedMessage")),
                    "Unexpected error message: " + loginPage.getErrorMessage());
        }
    }
}
