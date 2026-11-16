#!/usr/bin/env python3
"""Create the demo TestData.xlsx for CorpEquip Pay automation tests.
Usage: python scripts/create_testdata.py
Requires: pip install openpyxl
"""
import openpyxl
import os

def main():
    # Create testdata directory if it doesn't exist
    testdata_dir = os.path.join(os.path.dirname(__file__), '..', 'testdata')
    os.makedirs(testdata_dir, exist_ok=True)

    # Create workbook
    wb = openpyxl.Workbook()

    # Scenario 2 test data sheet
    ws = wb.active
    ws.title = "Scenario2"
    ws.append(['testId', 'description', 'year', 'expectedBehavior'])
    ws.append(['TC_SCENARIO2_01', 'Net vs Gross comparison for 2026 (bug OFF)', '2026', 'PASS'])

    # Set column widths
    for col in ws.columns:
        max_length = 0
        for cell in col:
            try:
                if len(str(cell.value)) > max_length:
                    max_length = len(str(cell.value))
            except:
                pass
        adjusted_width = (max_length + 2)
        ws.column_dimensions[col[0].column_letter].width = adjusted_width

    # Save file
    output_path = os.path.join(testdata_dir, 'TestData.xlsx')
    wb.save(output_path)
    print(f"Created {output_path}")

if __name__ == "__main__":
    main()