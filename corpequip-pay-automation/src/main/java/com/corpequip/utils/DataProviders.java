package com.corpequip.utils;

import org.testng.annotations.DataProvider;

/** TestNG data providers backed by the Excel workbook. */
public final class DataProviders {

    private DataProviders() { }

    @DataProvider(name = "scenario2Data", parallel = true)
    public static Object[][] scenario2Data() {
        // Scenario 2: Net vs Gross comparison (BUG-01)
        // Test data: year to compare, expected behavior
        return ExcelUtils.toDataProvider(ExcelUtils.readSheet(ConfigReader.get("testdata.file"), "Scenario2"));
    }
}