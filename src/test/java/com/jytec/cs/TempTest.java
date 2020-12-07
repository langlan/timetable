package com.jytec.cs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.service.DeptService;
import com.jytec.cs.service.ModelService;
import com.jytec.cs.service.api.ScheduleSearchParams;

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