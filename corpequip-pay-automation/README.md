# CorpEquip Pay Test Automation Framework

A Java Selenium TestNG framework for testing the CorpEquip Pay Merchant Hub application.

## Tech Stack

| Component | Version |
|-----------|---------|
| Java      | 17      |
| Maven     | 3.9+    |
| Selenium  | 4.25.0  |
| TestNG    | 7.10.2  |
| ExtentReports | 5.1.2 |
| Apache POI | 5.3.0  |
| Log4j2    | 2.23.1  |

## Project Structure

```
corpequip-pay-automation/
├── pom.xml                          # Maven build file
├── testng.xml                       # TestNG suite configuration
├── README.md                        # This file
├── .gitignore                       # Git ignore patterns
├── src/
│   ├── main/java/com/corpequip/
│   │   ├── base/
│   │   │   ├── BasePage.java       # Base page with common actions
│   │   │   └── DriverFactory.java    # ThreadLocal WebDriver management
│   │   ├── pages/
│   │   │   ├── Dashboard.java        # Dashboard page with Business Overview
│   │   │   ├── ReportsHubPage.java   # Entry point for reports
│   │   │   ├── TotalsSummaryPage.java # Totals Summary report
│   │   │   ├── SettledBatchesPage.java # Settled Batches report
│   │   │   └── SearchTransactionsPage.java # Search Transactions report
│   │   └── utils/
│   │       ├── ConfigReader.java     # Configuration management
│   │       ├── ExcelUtils.java       # Excel read/write utilities
│   │       ├── DataProviders.java    # TestNG data providers
│   │       └── ScreenshotUtils.java  # Screenshot capture
│   │   └── listeners/
│   │       ├── TestListener.java     # TestNG listener for reports
│   │       ├── ExtentManager.java    # ExtentReports manager
│   │       ├── RetryAnalyzer.java    # Test retry logic
│   │       └── RetryTransformer.java # Auto-apply retry to all tests
│   ├── test/java/com/corpequip/
│   │   └── tests/
│   │       ├── BaseTest.java         # Base test class
│   │       └── Scenario2Test.java  # Scenario 2 test
│   └── test/resources/
│       ├── config/
│       │   ├── config.properties
│       │   ├── config-qa.properties
│       │   └── log4j2.xml
├── testdata/
│   └── TestData.xlsx                 # Test data
├── scripts/
│   └── create_testdata.py            # Script to regenerate test data
└── output/                           # Generated output (git-ignored)
    ├── reports/ExtentReport.html
    ├── logs/automation.log
    ├── screenshots/
    └── excel/TestResults.xlsx
```

## Prerequisites

1. **Java 17+** - https://adoptium.net/
2. **Maven 3.9+** - https://maven.apache.org/
3. **Chrome or Firefox browser** - for test execution

## Setup

1. Clone or copy this project to your desired location
2. The application URL is configured in `src/test/resources/config/config.properties`:
   - Default: `file:///D:/AI/DemoFInapp/Resources/corpequip-payNew.html`
   - Update the path to match your local setup

3. Generate test data (if not already created):
   ```bash
   pip install openpyxl
   python scripts/create_testdata.py
   ```

## Run Commands

### Run all tests
```bash
mvn clean test
```

### Run specific test group (smoke/regression/api)
```bash
mvn clean test -Dgroups=smoke
mvn clean test -Dgroups=regression
```

### Run tests in headless mode
```bash
mvn clean test -Dheadless=true
```

### Run with specific browser
```bash
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=chrome
mvn clean test -Dbrowser=edge
```

### Run with different environment
```bash
mvn clean test -Denv=qa
```

### Run with retry
```bash
mvn clean test -Dretry.count=2
```

### Parallel execution
The default configuration runs tests in parallel (4 threads). Configure in `testng.xml`:
```xml
<suite parallel="methods" thread-count="4">
```

## Scenario 2: Net vs Gross Comparison (BUG-01)

### Test Description
This test validates the reconciliation between Totals Summary Report and Business Overview Dashboard.

### Bug Background
- **BUG-01**: When enabled, the Totals Summary report shows gross sales instead of net (sales - refunds)
- **Business Overview** always shows the correct net amount

### Expected Behavior (bug OFF)
- Totals Summary "Total Submitted Amount" = Business Overview "Batch Total"
- Both values are equal (net = gross - refunds)

### Bug Behavior (bug ON)
- Totals Summary shows gross sales (includes refunds)
- Business Overview shows net (sales - refunds)
- Values differ by the refund amount

### Test Execution

**Pass scenario (bug OFF):**
```
mvn clean test -Dtest=Scenario2Test
```
- Should PASS when visiting the app without bug flags

**Fail scenario (bug ON):**
1. Add `?bugs=BUG-01` to the URL before running the test
2. Expected: Test FAILS with clear evidence of the discrepancy

### Evidence Capture
The test captures:
- Test year
- Business Overview Batch Total (net)
- Totals Summary Total Submitted (should be net, but gross with bug)
- Difference between values
- Whether bug was detected

## Reports

### Extent Report
Open `output/reports/ExtentReport.html` in a browser after test execution.

### Excel Results
Test execution results are saved to `output/excel/TestResults.xlsx` with columns:
- Test
- Groups
- Status
- Duration(ms)
- Error

### Screenshots
Screenshots of failed tests are saved to `output/screenshots/`.

## Configuration

### Environment Variables
| Property | Default | Description |
|----------|---------|-------------|
| `env` | qa | Environment name (qa, staging, prod) |
| `base.url` | file:///... | Application URL |
| `browser` | chrome | Browser to use (chrome, firefox, edge) |
| `headless` | false | Run in headless mode |
| `explicit.wait.seconds` | 10 | WebDriverWait timeout |
| `retry.count` | 1 | Number of retry attempts |

### Test Data Format
The `TestData.xlsx` file contains sheets for each test type:

**Scenario2 sheet:**
```
testId | description | year | expectedBehavior
TC_SCENARIO2_01 | Net vs Gross comparison | 2026 | PASS
```

## Troubleshooting

### Test fails with "Element not found"
- Check if the correct URL is configured in `config.properties`
- Verify the application is accessible
- Try running with `headless=false` to debug visually

### Driver issues
- Selenium Manager should auto-download drivers
- If issues persist, manually download: chromedriver/geckodriver
- Ensure browser versions match driver versions

### Test data not found
- Run `python scripts/create_testdata.py`
- Verify Excel file exists in `testdata/` directory

## License
Internal use for CorpEquip Pay testing.