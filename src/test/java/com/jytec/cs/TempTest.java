package com.jytec.cs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.service.ModelService;
import com.jytec.cs.service.api.ScheduleSearchParams;
import com.jytec.cs.service.DeptService;

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
				// ================================================================================================== //
				// CellAdress:
				// -- getRow, getColumn - get the cell location. 0 based index.
				// -- toString() gives a 'A1' (first column, first row) style location.
				// ---- witch use CellReference.convertNumToColString(colIndex) to covert the colIndex to String
				// ---- representation.
				// ================================================================================================== //
				CellAddress address = cell.getAddress();

				System.out.println(
						"merged-region - " + ii + " - " + region.formatAsString() + " - " + cell + " @ " + address);
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

	@Test
	public void testMerging() throws EncryptedDocumentException, IOException {
		Workbook wb = WorkbookFactory.create(new File("C:/Users/langlan/Desktop/test-merging.xlsx"));
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);

			int imr = sheet.getNumMergedRegions();
			System.out.println("merged-regions:" + imr);
			for (int ii = 0; ii < imr; ii++) {
				CellRangeAddress region = sheet.getMergedRegion(ii);
				Cell cell = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
				CellAddress address = cell.getAddress();

				System.out.println(
						"merged-region - " + ii + " - " + region.formatAsString() + " - " + cell + " @ " + address);
			}
			System.out.println("=============================================");
			for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);
				for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
					Cell cell = row.getCell(cellNum);
					System.out.print(cell + " - sheet.getRow(" + rowNum + ").getCell(" + cellNum + ") @ "
							+ (cell == null ? "null" : cell.getAddress()));
					System.out.print(" | ");
				}
				System.out.println();
			}
			System.out.println("=============================================");
			Cell cell = SheetUtil.getCell(sheet, 2, 1);
			System.out.println("cell(2, 1) 【" + cell + "】 @ " + (cell == null ? "" : cell.getAddress()));
			cell = getCellWithMerges(sheet, 2, 1);
			System.out.println("cell(2, 1) 【" + cell + "】 @ " + (cell == null ? "" : cell.getAddress()));
		}
	}

	// copy from SheetUtil
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

	@Test
	public void testJson() throws JsonProcessingException {
		Major m = new Major();
		m.setId(1);
		Dept dept = new Dept();
		dept.setId(1);
		m.setDept(dept);
		System.out.println(new ObjectMapper().writeValueAsString(m));
	}

	@Test
	public void testGeneric()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = (Class<?>) ((ParameterizedType) DeptService.class.getGenericSuperclass())
				.getActualTypeArguments()[0];
		assertEquals(Dept.class, clazz);

		DeptService t = new DeptService();
		Field clazzField = ModelService.class.getDeclaredField("clazz");
		clazzField.setAccessible(true);
		assertEquals(Dept.class, clazzField.get(t));
	}
	
	@Test
	public void testFieldNames()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field[] fields = ScheduleSearchParams.class.getDeclaredFields();
		for(Field f : fields) {
			System.out.print(f.getName() + ",");
		}
	}

}