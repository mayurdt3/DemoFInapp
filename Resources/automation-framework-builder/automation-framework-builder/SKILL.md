---
name: automation-framework-builder
description: Interviews the user about their project and tech stack, then generates a complete, runnable end-to-end test automation framework (Page Object Model, OOP) with a demo test, reporting, logging, screenshots, Excel data utilities, config files, CI pipelines and a README. Supports Java + Selenium + TestNG + Maven and Playwright (JavaScript or TypeScript). Use this skill whenever the user wants to create, scaffold, bootstrap, set up or standardize a test automation framework, QA automation project, Selenium or Playwright project, or POM-based UI/API test suite, even if they do not say "framework" explicitly.
---

# Automation Framework Builder

Generate a production-style test automation framework after interviewing the user. Output must be consistent between runs, so build from the bundled templates in `assets/` instead of writing code from scratch.

## Workflow

### 1. Grill the user (do not skip)
Read `references/grill-questions.md` and run the interview.
- Ask 5-8 core questions, in batches of 2-3 (not one giant list).
- Ask follow-ups only where an answer is vague, contradictory, or missing something the framework needs.
- Offer a default with every question so the user can answer "default".
- Before generating, show a short summary of decisions and get a yes. If the user already supplied answers in their first message, do not re-ask them.

### 2. Pick the stack and read its reference
- Java + Selenium: read `references/java-selenium.md`, copy `assets/java-selenium/`.
- Playwright: read `references/playwright-js.md`, copy `assets/playwright-js/`. If the user chose TypeScript, follow the TypeScript section in that reference.
- Always also read `references/standards.md` (shared rules, README template) and `references/ci-cd.md`.

### 3. Generate
- Copy the template tree into the user's chosen project folder, replacing the `{{PLACEHOLDERS}}` listed in the reference (project name, base URL, package/group id, etc.).
- Keep the demo test: SauceDemo login -> add to cart -> assert, data-driven from the Excel file.
- If the user gave their own application and flows, add a clearly marked starter page class and test with `TODO` locators. Never invent locators for an app you cannot see.
- Generate the test data workbook with `scripts/make_testdata.py` (creates `TestData.xlsx`).
- Generate the CI files the user chose and a README with exact run commands.

### 4. Verify
- Run `scripts/check_env.sh <java|playwright>` to see which prerequisites exist.
- If prerequisites and network access exist, run the demo test and report the real result. Otherwise run what is possible (syntax check, unit-level check of the Excel utility) and say plainly what was not run and why. Never claim the demo passed without running it.

### 5. Deliver
Give the user the project folder (or zip) and a short summary: structure, how to run, how to read reports, what to customize first.

## Non-negotiable standards
- Page Object Model: pages hold locators and actions, tests hold assertions and flow. No locators in tests, no assertions in pages (except tiny state getters like `isDisplayed`).
- OOP: base classes, encapsulation, one responsibility per class, interfaces or abstract classes where useful.
- No hard-coded sleeps; use explicit/auto waits.
- No credentials in code; read from config/env, with the SauceDemo public demo credentials allowed only in test data.
- Thread-safe browser handling so parallel runs work.
- Every failure produces a screenshot, a log entry and a report entry.
- All outputs land in `output/` (reports, logs, screenshots, results excel) and `output/` is git-ignored.
- Retry, tagging (smoke/regression/api), parallel execution and one API test are included.
- Check current stable dependency versions when web access exists; otherwise use the baseline versions in the references.
