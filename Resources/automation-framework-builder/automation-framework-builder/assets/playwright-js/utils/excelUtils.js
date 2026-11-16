import ExcelJS from 'exceljs';
import fs from 'node:fs';
import path from 'node:path';

/** Excel read/write helpers (.xlsx). Reads are row-objects keyed by header. */
export class ExcelUtils {
  /** Read a sheet into an array of objects, e.g. [{ username: 'a', password: 'b' }]. */
  static async readSheet(filePath, sheetName) {
    const workbook = new ExcelJS.Workbook();
    await workbook.xlsx.readFile(filePath);
    const sheet = workbook.getWorksheet(sheetName);
    if (!sheet) throw new Error(`Sheet "${sheetName}" not found in ${filePath}`);

    const headers = [];
    sheet.getRow(1).eachCell((cell, col) => { headers[col] = String(cell.value).trim(); });

    const rows = [];
    sheet.eachRow((row, rowNumber) => {
      if (rowNumber === 1) return;
      const record = {};
      headers.forEach((header, col) => {
        if (!header) return;
        const value = row.getCell(col).value;
        record[header] = value === null || value === undefined ? '' : String(value);
      });
      rows.push(record);
    });
    return rows;
  }

  /** Write (or overwrite) a sheet with headers and rows into a workbook file. */
  static async writeSheet(filePath, sheetName, headers, rows) {
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    const workbook = new ExcelJS.Workbook();
    if (fs.existsSync(filePath)) await workbook.xlsx.readFile(filePath);
    const existing = workbook.getWorksheet(sheetName);
    if (existing) workbook.removeWorksheet(existing.id);

    const sheet = workbook.addWorksheet(sheetName);
    sheet.addRow(headers).font = { bold: true };
    rows.forEach((row) => sheet.addRow(row));
    sheet.columns.forEach((column) => { column.width = 28; });
    await workbook.xlsx.writeFile(filePath);
  }
}
