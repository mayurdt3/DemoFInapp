import { test, expect } from '../../fixtures/baseTest.js';
import { ExcelUtils } from '../../utils/excelUtils.js';
import { config } from '../../utils/config.js';

// Data-driven: one test per row of the "Login" sheet in TestData.xlsx
const rows = await ExcelUtils.readSheet(config.testDataFile, 'Login');

test.describe('Login', () => {
  for (const row of rows) {
    test(`${row.testId} login as ${row.username} expects ${row.expectedResult} @smoke @regression`, async ({ loginPage }) => {
      await loginPage.load();

      if (row.expectedResult === 'SUCCESS') {
        const products = await loginPage.loginAs(row.username, row.password);
        expect(await products.getTitle()).toBe('Products');
      } else {
        await loginPage.login(row.username, row.password);
        expect(await loginPage.getErrorMessage()).toContain(row.expectedMessage);
      }
    });
  }
});
