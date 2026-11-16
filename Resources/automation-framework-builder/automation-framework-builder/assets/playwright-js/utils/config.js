import dotenv from 'dotenv';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const envName = process.env.ENV || 'qa';
// Environment file first, then real environment variables win (CI secrets).
dotenv.config({ path: path.join(root, 'config', `${envName}.env`) });

/** Single source of configuration. Secrets must come from environment variables. */
export const config = Object.freeze({
  env: envName,
  root,
  baseUrl: process.env.BASE_URL,
  apiBaseUrl: process.env.API_BASE_URL,
  headless: process.env.HEADLESS !== 'false',
  browser: process.env.BROWSER || 'all', // chromium | firefox | webkit | all
  workers: Number(process.env.WORKERS) || (process.env.CI ? 4 : undefined),
  retries: Number(process.env.RETRIES ?? (process.env.CI ? 2 : 1)),
  dirs: {
    output: path.join(root, 'output'),
    reports: path.join(root, 'output', 'reports'),
    logs: path.join(root, 'output', 'logs'),
    screenshots: path.join(root, 'output', 'screenshots'),
    excel: path.join(root, 'output', 'excel'),
  },
  testDataFile: path.join(root, 'testdata', 'TestData.xlsx'),
});
