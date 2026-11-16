#!/usr/bin/env python3
"""Create the demo TestData.xlsx (SauceDemo) used by both stacks.
Usage: python3 make_testdata.py <output_path>   (default: testdata/TestData.xlsx)
Requires: pip install openpyxl
"""
import os
import sys
from openpyxl import Workbook

out = sys.argv[1] if len(sys.argv) > 1 else "testdata/TestData.xlsx"
os.makedirs(os.path.dirname(out) or ".", exist_ok=True)

wb = Workbook()
ws = wb.active
ws.title = "Login"
ws.append(["testId", "username", "password", "expectedResult", "expectedMessage"])
ws.append(["TC_LOGIN_01", "standard_user", "secret_sauce", "SUCCESS", ""])
ws.append(["TC_LOGIN_02", "locked_out_user", "secret_sauce", "ERROR", "locked out"])
ws.append(["TC_LOGIN_03", "standard_user", "wrong_password", "ERROR", "do not match"])

cart = wb.create_sheet("Cart")
cart.append(["testId", "username", "password", "productId", "expectedCartCount"])
cart.append(["TC_CART_01", "standard_user", "secret_sauce", "sauce-labs-backpack", "1"])
cart.append(["TC_CART_02", "standard_user", "secret_sauce", "sauce-labs-bike-light", "1"])

for sheet in wb.worksheets:
    for col in sheet.columns:
        sheet.column_dimensions[col[0].column_letter].width = 24
wb.save(out)
print(f"Created {out}")
