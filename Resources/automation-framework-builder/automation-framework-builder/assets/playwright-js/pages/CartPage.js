import { BasePage } from './BasePage.js';

export class CartPage extends BasePage {
  #itemNames = this.page.locator('[data-test="inventory-item-name"]');

  async getItemNames() {
    return this.#itemNames.allTextContents();
  }
}
