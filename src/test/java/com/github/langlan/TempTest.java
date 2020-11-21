package com.github.langlan;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

import com.github.langlan.util.Dates;
import com.github.langlan.util.Dates.CalenderWrapper;

public class TempTest {
	@Test
	public void test() throws IOException {
		String filename = "C:/Users/langlan/Desktop/课表/实训课表/20-21-1工程系实训课表（2020.10.15）.xlsx";
		Workbook wb = WorkbookFactory.create(new File(filename), null, true);
		System.out.println(wb.getNumberOfSheets());

		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);

			// check merged regions.
			int imr = sheet.getNumMergedRegions();
			System.out.println("merged-regions:" + imr);
			for (int ii = 0; ii < imr; ii++) {
				CellRangeAddress region = sheet.getMergedRegion(ii);
				Cell cell = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
				CellAddress adress = cell.getAddress();
				System.out.println("merged-region - " + ii + " - " + region.formatAsString() + " - " + cell);
			}

		}

		wb.close();
	}

	void print(Row row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			sb.append(row.getCell(i));
			sb.append(" | ");
		}
		System.out.println(sb);
	}
	
}
