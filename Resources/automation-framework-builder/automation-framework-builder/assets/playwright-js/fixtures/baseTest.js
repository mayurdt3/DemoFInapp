import { test as base, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage.js';
import { ScreenshotUtils } from '../utils/screenshotUtils.js';
import { logger } from '../utils/logger.js';

/**
 * Extended test: injects page objects and an automatic hook that logs
 * start/end and saves a screenshot into output/screenshots on failure.
 */
export const test = base.extend({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },

  // auto: true => runs for every test that uses this fixture file
  _lifecycle: [
    async ({ page }, use, testInfo) => {
      logger.info(`START ${testInfo.title} [${testInfo.project.name}]`);
      await use();
      const failed = testInfo.status !== testInfo.expectedStatus;
      if (failed) {
        const file = await ScreenshotUtils.capture(page, testInfo.title);
        await testInfo.attach('failure-screenshot', { path: file, contentType: 'image/png' });
        logger.error(`FAILED ${testInfo.title}: ${testInfo.error?.message?.split('\n')[0]}`);
      }
      logger.info(`END ${testInfo.title} -> ${testInfo.status}`);
    },
    { auto: true },
  ],
});

export { expect };
