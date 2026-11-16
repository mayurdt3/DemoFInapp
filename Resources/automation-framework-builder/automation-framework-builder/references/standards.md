# Shared Standards

## Folder layout (both stacks)
```
<project>/
  pages/ or src/main/java/.../pages   page classes
  tests/ or src/test/java/...         test classes/specs (ui + api)
  utils/                              config, logger, excel, screenshot helpers
  config/ or src/test/resources       properties/.env, log config, testng.xml
  testdata/                           TestData.xlsx
  output/                             reports/ logs/ screenshots/ excel/   (git-ignored)
  Jenkinsfile, .github/workflows/
  README.md, .gitignore
```

## OOP and design rules
- Abstraction: `BasePage` (click, type, getText, waits) and `BaseTest` (setup/teardown).
- Inheritance: every page extends `BasePage`; every Java test extends `BaseTest`.
- Encapsulation: locators are private; expose behavior (`login(user, pass)`), not elements.
- Composition: tests compose page objects; pages return the next page (`login()` returns `ProductsPage`).
- Factory: `DriverFactory` / browser fixture picks the browser from config.
- Single source of config: one `ConfigReader`; environment chosen by `-Denv=qa` (Java) or `ENV=qa` (JS).

## Test design
- Tags: `smoke`, `regression`, `api`. TestNG groups or Playwright `--grep`.
- Retry: 1 retry locally, 2 in CI; retried failures visible in the report.
- Independent tests: each test gets a clean browser context, safe to run in parallel.
- Naming: pages end in `Page`; Java tests end in `Test`; JS specs end in `.spec`.

## Demo data (SauceDemo, public)
- URL: https://www.saucedemo.com
- Valid: `standard_user` / `secret_sauce`; locked: `locked_out_user` / `secret_sauce`; bad: any wrong pair.
- Bad/locked login error starts with "Epic sadface".
- Add-to-cart button id: `add-to-cart-sauce-labs-backpack`; cart badge `.shopping_cart_badge`; cart link `.shopping_cart_link`.
- API demo target: https://jsonplaceholder.typicode.com (GET /posts/1 returns id 1).

## README template (generate for every project)
Sections: Overview, Tech stack, Folder structure, Prerequisites, Setup, Configuration (envs, secrets), Run commands (all, by tag, by browser, parallel, headed/headless), Reports/logs/screenshots locations, Excel data format, How to add a page + test (step by step), CI usage, Troubleshooting, Coding standards.
