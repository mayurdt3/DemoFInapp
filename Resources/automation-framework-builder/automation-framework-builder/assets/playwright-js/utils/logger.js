import winston from 'winston';
import fs from 'node:fs';
import { config } from './config.js';

fs.mkdirSync(config.dirs.logs, { recursive: true });

const line = winston.format.printf(
  ({ level, message, timestamp }) => `${timestamp} [${level.toUpperCase()}] ${message}`
);

/** Shared Winston logger: console + output/logs/automation.log */
export const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss.SSS' }), line),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: `${config.dirs.logs}/automation.log` }),
  ],
});
