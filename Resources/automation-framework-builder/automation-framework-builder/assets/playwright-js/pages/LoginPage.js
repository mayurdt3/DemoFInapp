import { BasePage } from './BasePage.js';
import { ProductsPage } from './ProductsPage.js';

export class LoginPage extends BasePage {
  // Locators are private (encapsulation): tests use behavior, not elements.
  #username = this.page.locator('[data-test="username"]');
  #password = this.page.locator('[data-test="password"]');
  #loginButton = this.page.locator('[data-test="login-button"]');
  #error = this.page.locator('[data-test="error"]');

  async load() {
    await this.open('/');
    return this;
  }

  async login(username, password) {
    await this.type(this.#username, username, 'username');
    await this.type(this.#password, password, 'password');
    await this.click(this.#loginButton, 'login button');
  }

  /** Successful login navigates to the products page. */
  async loginAs(username, password) {
    await this.login(username, password);
    return new ProductsPage(this.page);
  }

  async getErrorMessage() {
    return this.getText(this.#error);
  }
}
