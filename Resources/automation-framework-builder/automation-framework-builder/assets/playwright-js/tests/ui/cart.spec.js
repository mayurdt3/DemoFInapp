import { test, expect } from '../../fixtures/baseTest.js';
import { ExcelUtils } from '../../utils/excelUtils.js';
import { config } from '../../utils/config.js';

// Data-driven: one test per row of the "Cart" sheet. Flow: login -> add to cart -> assert
const rows = await ExcelUtils.readSheet(config.testDataFile, 'Cart');

test.describe('Cart', () => {
  for (const row of rows) {
    test(`${row.testId} add ${row.productId} to cart @smoke @regression`, async ({ loginPage }) => {
      await loginPage.load();
      const products = await loginPage.loginAs(row.username, row.password);

      await products.addToCart(row.productId);
      expect(await products.getCartCount()).toBe(Number(row.expectedCartCount));

      const cart = await products.openCart();
      const items = await cart.getItemNames();
      expect(items.length).toBe(Number(row.expectedCartCount));
    });
  }
});
