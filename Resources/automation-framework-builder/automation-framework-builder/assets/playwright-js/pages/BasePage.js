/** Abstract base for every page object: common, reusable actions with logging. */
import { logger } from '../utils/logger.js';

export class BasePage {
  constructor(page) {
    if (new.target === BasePage) throw new TypeError('BasePage is abstract');
    this.page = page;
  }

  async open(path = '/') {
    logger.info(`Open ${path}`);
    await this.page.goto(path);
  }

  async click(locator, description = 'element') {
    logger.info(`Click ${description}`);
    await locator.click();
  }

  async type(locator, text, description = 'field') {
    logger.info(`Type into ${description}`);
    await locator.fill(text);
  }

  async getText(locator) {
    return (await locator.textContent())?.trim() ?? '';
  }

  async isVisible(locator) {
    return locator.isVisible();
  }
}
