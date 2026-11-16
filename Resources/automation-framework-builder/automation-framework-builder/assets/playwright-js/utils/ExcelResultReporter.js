import path from 'node:path';
import { ExcelUtils } from './excelUtils.js';
import { config } from './config.js';

/**
 * Custom reporter: runs once in the main process after all workers finish,
 * so parallel workers never write the same file at the same time.
 */
export default class ExcelResultReporter {
  #rows = [];

  onTestEnd(test, result) {
    this.#rows.push([
      test.title,
      test.parent.project()?.name ?? '',
      result.status,
      result.retry,
      Math.round(result.duration),
      (result.error?.message ?? '').split('\n')[0],
    ]);
  }

  async onEnd() {
    const file = path.join(config.dirs.excel, 'TestResults.xlsx');
    await ExcelUtils.writeSheet(
      file,
      'Results',
      ['Test', 'Browser', 'Status', 'Retry', 'Duration(ms)', 'Error'],
      this.#rows
    );
  }
}
