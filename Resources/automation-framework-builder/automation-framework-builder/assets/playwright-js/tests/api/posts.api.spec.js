import { test, expect } from '@playwright/test';
import { config } from '../../utils/config.js';
import { logger } from '../../utils/logger.js';

test.describe('Posts API', () => {
  test('GET /posts/1 returns the post @api @smoke', async ({ request }) => {
    const response = await request.get(`${config.apiBaseUrl}/posts/1`);
    logger.info(`GET /posts/1 -> ${response.status()}`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBe(1);
    expect(body).toHaveProperty('title');
  });

  test('POST /posts creates a post @api @regression', async ({ request }) => {
    const response = await request.post(`${config.apiBaseUrl}/posts`, {
      data: { title: 'qa', body: 'demo', userId: 1 },
    });
    expect(response.status()).toBe(201);
    expect((await response.json()).title).toBe('qa');
  });
});
