import { BasePage } from './BasePage.js';
import { CartPage } from './CartPage.js';

export class ProductsPage extends BasePage {
  #title = this.page.locator('[data-test="title"]');
  #cartBadge = this.page.locator('[data-test="shopping-cart-badge"]');
  #cartLink = this.page.locator('[data-test="shopping-cart-link"]');

  async getTitle() {
    return this.getText(this.#title);
  }

  async addToCart(productId) {
    await this.click(this.page.locator(`[data-test="add-to-cart-${productId}"]`), `add ${productId}`);
  }

  /** Returns 0 when the badge is not shown (empty cart). */
  async getCartCount() {
    if (!(await this.isVisible(this.#cartBadge))) return 0;
    return Number(await this.getText(this.#cartBadge));
  }

  async openCart() {
    await this.click(this.#cartLink, 'cart');
    return new CartPage(this.page);
  }
}
