# Playwright (JavaScript or TypeScript)

Template: `assets/playwright-js/` (JavaScript, ES modules; copy the whole tree including hidden `.gitignore`).
The user must be asked JavaScript or TypeScript. Never choose silently.

## Placeholders to replace
- `{{PROJECT_NAME}}` in `package.json`

## Stack
Node 20+, `@playwright/test`, `exceljs` (Excel), `winston` (logs), `dotenv` (config). HTML report built in. Baseline versions are in `package.json`; check latest stable if web access exists.

## Layout
```
config/       qa.env, staging.env            (BASE_URL, API_BASE_URL; secrets via real env vars)
pages/        BasePage (abstract), LoginPage, ProductsPage, CartPage   (private #locators)
fixtures/     baseTest.js  (injects page objects, logs, failure screenshot)
utils/        config.js, logger.js, excelUtils.js, screenshotUtils.js, ExcelResultReporter.js
tests/ui/     login.spec.js, cart.spec.js    (data-driven from Excel)
tests/api/    posts.api.spec.js              (request fixture)
testdata/     TestData.xlsx   scripts/make_testdata.py   playwright.config.js
output/       reports/html, logs/automation.log, screenshots/, excel/TestResults.xlsx, test-results/ (traces, videos)
```

## Run commands (put these in the README)
```
npm install && npx playwright install      # first time
npm test                                    # all tests, chromium + firefox
npm run test:smoke | test:regression | test:api
ENV=staging BROWSER=firefox HEADLESS=false npx playwright test
WORKERS=4 RETRIES=2 npx playwright test
npm run report                              # open the HTML report
```
Tags are written in test titles (`@smoke @regression @api`) and selected with `--grep`.
Parallel: `fullyParallel: true`; workers default to 4 on CI (`CI=true`), otherwise Playwright's default.

## TypeScript variant (only if the user chose TypeScript)
- Rename `.js` to `.ts` (config, pages, fixtures, utils, specs); keep `"type": "module"` or switch to CommonJS, but be consistent.
- Add `typescript` and `@types/node` as dev dependencies and a `tsconfig.json` (`"target": "ES2022"`, `"module": "ESNext"`, `"moduleResolution": "Bundler"`, `"strict": true`).
- Type the page constructors (`constructor(protected readonly page: Page)`), use `private readonly` locators instead of `#fields`, and use an `interface LoginRow { testId: string; ... }` for Excel rows.
- Relative imports in ESM TypeScript may need explicit `.js` suffixes; if Playwright's loader complains, drop `"type": "module"`.

## Rules specific to this stack
- Use Playwright locators and auto-waiting; no `waitForTimeout`.
- Specs read Excel with top-level `await` (works because the project is ESM).
- Excel results are written by `ExcelResultReporter` in `onEnd`, so parallel workers never write the same file.
- Tests that need a browser import `test` from `fixtures/baseTest.js`; API tests import from `@playwright/test` directly so they do not open a browser.
- Credentials for real applications come from environment variables or CI secrets, never from `config/*.env` committed files.

## Verification
Run `scripts/check_env.sh playwright`. Always run `npx playwright test --list` (works without browsers) to prove specs and Excel data load. If browsers and network exist, run `npx playwright test --grep @smoke` and report the real outcome. Otherwise say it was not run.
