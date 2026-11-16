package com.automation.utils;

import org.testng.annotations.DataProvider;

/** TestNG data providers backed by the Excel workbook. */
public final class DataProviders {

    private DataProviders() { }

    @DataProvider(name = "loginData", parallel = true)
    public static Object[][] loginData() {
        return ExcelUtils.toDataProvider(ExcelUtils.readSheet(ConfigReader.get("testdata.file"), "Login"));
    }

    @DataProvider(name = "cartData", parallel = true)
    public static Object[][] cartData() {
        return ExcelUtils.toDataProvider(ExcelUtils.readSheet(ConfigReader.get("testdata.file"), "Cart"));
    }
}
