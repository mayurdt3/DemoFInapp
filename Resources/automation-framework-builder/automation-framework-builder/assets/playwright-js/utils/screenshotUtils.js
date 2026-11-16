import fs from 'node:fs';
import path from 'node:path';
import { config } from './config.js';

export class ScreenshotUtils {
  /** Save a full-page screenshot into output/screenshots and return the file path. */
  static async capture(page, name) {
    fs.mkdirSync(config.dirs.screenshots, { recursive: true });
    const safe = name.replace(/[^a-z0-9_-]+/gi, '_').slice(0, 100);
    const file = path.join(config.dirs.screenshots, `${safe}_${Date.now()}.png`);
    await page.screenshot({ path: file, fullPage: true });
    return file;
  }
}
