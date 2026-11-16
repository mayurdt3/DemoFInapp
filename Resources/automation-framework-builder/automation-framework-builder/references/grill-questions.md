# Grill Questions

Ask in batches. Each question has a default. Skip anything the user already answered.

## Core (ask all, 5-8)
1. **Project**: What application are you testing (name, URL, web/API)? Is this a brand-new framework or are we adding to an existing repo? (default: new, web UI + API)
2. **Stack**: Java + Selenium (Maven, TestNG) or Playwright? If Playwright: JavaScript or TypeScript? (no silent choice, always ask)
3. **Scope**: Which flows matter first (login, search, checkout, forms...)? Roughly how many pages/screens? (default: login + one core flow)
4. **Environments**: How many environments (dev/qa/staging/prod) and how are URLs/credentials supplied? (default: qa + staging via properties/.env, secrets from environment variables)
5. **Browsers and execution**: Which browsers, headless or headed, parallel threads? (default: Chrome + Firefox, headless in CI, 4 parallel threads)
6. **Test data**: Excel only, or also JSON/DB? Who maintains the data? (default: Excel, .xlsx)
7. **CI/CD**: Jenkins, GitHub Actions, both, none? (default: both)
8. **Reporting**: Any report tool already in use or required (Allure, Extent, HTML)? Where are reports published? (default: Extent for Java, Playwright HTML for JS)

## Follow-ups (only if vague)
- Login complexity: SSO, MFA, captcha, API token available to bypass UI login?
- Test accounts: do they exist per role? How are they reset?
- Existing conventions: naming, package structure, lint rules to follow?
- Team skill level and who maintains the framework (affects how much abstraction to add).
- API: base URL, auth type (Bearer/API key/OAuth), Swagger/OpenAPI spec available?
- Grid/cloud: BrowserStack, Sauce Labs, Selenium Grid, Docker?
- Compliance: must logs mask sensitive data?

## After answers
Print a summary table: Project, Stack, Language, Environments, Browsers, Parallelism, Data, CI, Reporting, API scope. Ask: "Shall I generate this?" Generate only after a yes.
