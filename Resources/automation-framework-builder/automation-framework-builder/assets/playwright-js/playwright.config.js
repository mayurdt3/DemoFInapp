import { defineConfig, devices } from '@playwright/test';
import { config } from './utils/config.js';

const allProjects = [
  { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  { name: 'webkit', use: { ...devices['Desktop Safari'] } },
];
const projects = config.browser === 'all'
  ? allProjects.filter((p) => p.name !== 'webkit') // default: chromium + firefox
  : allProjects.filter((p) => p.name === config.browser);

export default defineConfig({
  testDir: './tests',
  outputDir: 'output/test-results',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: config.retries,
  workers: config.workers,
  timeout: 30_000,
  expect: { timeout: 7_000 },
  reporter: [
    ['list'],
    ['html', { outputFolder: 'output/reports/html', open: 'never' }],
    ['./utils/ExcelResultReporter.js'],
  ],
  use: {
    baseURL: config.baseUrl,
    headless: config.headless,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
  },
  projects,
});
