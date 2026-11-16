# Java + Selenium + TestNG + Maven

Template: `assets/java-selenium/` (copy the whole tree, including hidden `.gitignore`).

## Placeholders to replace
- `{{PROJECT_NAME}}` in `pom.xml`, `testng.xml`, `ExtentManager.java` (report name)
- `{{GROUP_ID}}` in `pom.xml` (e.g. `com.acme`)
- Base package is `com.automation`. If the user wants another package, rename the folders under `src/main/java`, `src/test/java` and update `package`/`import` lines, `testng.xml` and `log4j2.xml` if needed.

## Stack
Java 17, Maven, Selenium 4 (Selenium Manager downloads drivers, no WebDriverManager), TestNG, Extent Reports 5 (Spark), Log4j2, Apache POI (.xlsx), REST Assured (API).
Baseline versions are in `pom.xml`. If web access exists, check the latest stable versions and bump them; otherwise keep the baseline.

## Layout
```
src/main/java/com/automation/
  base/       BasePage (explicit waits, logged actions), DriverFactory (ThreadLocal)
  pages/      LoginPage, ProductsPage, CartPage  (private locators, behavior methods)
  utils/      ConfigReader, ExcelUtils, DataProviders, ScreenshotUtils
  listeners/  TestListener, ExtentManager, RetryAnalyzer, RetryTransformer
src/test/java/com/automation/
  tests/      BaseTest, LoginTest, CartTest        (UI, data-driven from Excel)
  api/        PostsApiTest                          (REST Assured)
src/test/resources/  config/*.properties, log4j2.xml
testdata/TestData.xlsx    testng.xml    pom.xml
output/     reports/ExtentReport.html, logs/automation.log, screenshots/, excel/TestResults.xlsx
```

## Run commands (put these in the README)
```
mvn clean test                                   # everything, defaults from config.properties
mvn clean test -Dgroups=smoke                    # by tag (smoke | regression | api)
mvn clean test -Dbrowser=firefox -Dheadless=true
mvn clean test -Denv=staging -Dretry.count=2
```
Parallelism: edit `thread-count` / `data-provider-thread-count` in `testng.xml` (default 4). Data providers are `parallel = true`.
If `-Dgroups` is ignored by the Surefire version in use, add `<groups>${groups}</groups>` inside the Surefire `<configuration>`.

## Adding a page and a test
1. New class in `pages/` extending `BasePage`; private `By` locators; public behavior methods that return the next page object.
2. New test class in `src/test/java/.../tests/` extending `BaseTest`; assertions only in tests; add `groups = {...}`.
3. Data-driven: add a sheet to `TestData.xlsx` (row 1 = headers, include `testId`), add a `@DataProvider` in `DataProviders`.

## Rules specific to this stack
- Never call `Thread.sleep`; add waits to `BasePage` instead.
- Always get the driver from `DriverFactory`, never a static shared field.
- `RetryTransformer` already applies retry to all tests; do not add `retryAnalyzer` per test.
- Credentials for real applications come from `-D` properties or environment variables, never from committed files.
- The test results workbook is a copy in `output/excel/`; never overwrite `testdata/TestData.xlsx` from tests.
- Create the workbook with `python3 scripts/make_testdata.py testdata/TestData.xlsx` (needs `pip install openpyxl`). If Python is unavailable, create it in Java with `ExcelUtils.writeSheet`.

## Verification
Run `scripts/check_env.sh java`. If Java 17, Maven, a browser and network access all exist, run `mvn -B clean test -Dgroups=smoke -Dheadless=true` and report the real outcome. Otherwise say it was not run.
