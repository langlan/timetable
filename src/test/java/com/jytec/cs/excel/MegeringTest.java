package com.jytec.cs.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetUtil;
import org.junit.jupiter.api.Test;

public class MegeringTest {
	void print(Row row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			sb.append(row.getCell(i));
			sb.append(" | ");
		}
		System.out.println(sb);
	}

	@Test
	public void testMerging() throws EncryptedDocumentException, IOException {
		try (InputStream is = getClass().getResourceAsStream("/test-merging.xlsx");
				Workbook wb = WorkbookFactory.create(is);) {
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				Sheet sheet = wb.getSheetAt(i);

				int imr = sheet.getNumMergedRegions();
				assertEquals(2, imr); // we have 2 merged cell.
				System.out.println("merged-regions:" + imr);
				for (int ii = 0; ii < imr; ii++) {
					CellRangeAddress region = sheet.getMergedRegion(ii);
					Cell cell = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
					// address didn't take the merging information.
					CellAddress address = cell.getAddress();

					System.out.println(
							"merged-region " + ii + " - " + region.formatAsString() + " - " + cell + " @ " + address);
				}
				System.out.println("============================================================");
				for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
					Row row = sheet.getRow(rowNum);
					for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
						StringBuilder sb = new StringBuilder();
						Cell cell = row.getCell(cellNum);
						String cellStr = (cell == null ? "XXXX" : cell.toString());
						String cellIdx = "(" + rowNum + ", " + cellNum + ")";
						String cellAddress = (cell == null ? "XX" : cell.getAddress().toString());
						String cellType = cell != null ? cell.getCellType().toString() : "NULL_CELL";

						sb.append(cellAddress).append(cellIdx);
						sb.append("[").append(fixedWidth(cellStr, 4)).append("]");
						sb.append(fixedWidth(cellType, 9));

						System.out.print(sb);
						System.out.print("   |   ");
					}
					System.out.println();
				}
				System.out.println("------------------------------------------------------------");
				System.out.println("We see B1, C1 are not-null but blank. which merged in A1:C1");
				System.out.println("We see B3, B4 are not-null but blank. which merged in B2:B4");
				System.out.println("============================================================");
				System.out.println("So we can not determine a cell is not merged only by it is not null.");
				Cell cell = SheetUtil.getCellWithMerges(sheet, 2, 1);
				System.out.println("B3(2, 1) 【" + cell + "】 @ " + (cell == null ? "" : cell.getAddress()));
				cell = getCellWithMerges(sheet, 2, 1);
				System.out.println("B3(2, 1) 【" + cell + "】 @ " + (cell == null ? "" : cell.getAddress()));
			}

		}
	}

	private String fixedWidth(String cellStr, int width) {
		String padding = "                               ";
		if (cellStr.length() < width)
			return cellStr + padding.substring(0, width - cellStr.length());
		return cellStr;
	}

	// copy&refine from SheetUtil
	private Cell getCellWithMerges(Sheet sheet, int rowIx, int colIx) {
		for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
			if (mergedRegion.isInRange(rowIx, colIx)) {
				// The cell wanted is in this merged range
				// Return the primary (top-left) cell for the range
				Row r = sheet.getRow(mergedRegion.getFirstRow());
				if (r != null) {
					return r.getCell(mergedRegion.getFirstColumn());
				}
			}
		}
		return sheet.getRow(rowIx).getCell(colIx);
	}
}
