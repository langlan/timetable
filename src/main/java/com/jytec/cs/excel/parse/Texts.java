package com.jytec.cs.excel.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public interface Texts {
	Pattern INTTEGER = Pattern.compile("\\d+");

	/** find the first digital sequence and parse or 0 */
	static int firstInt(String text) {
		Matcher m = INTTEGER.matcher(text);
		if (m.find()) {
			return Integer.parseInt(m.group());
		}
		return 0;
	}

	/** find the first digital sequence or null */
	static String firstIntStr(String text) {
		Matcher m = INTTEGER.matcher(text);
		if (m.find()) {
			return m.group();
		}
		return null;
	}

	/** always return a not null string, trimed */
	static String cellString(Cell cell) {
		String text = cell != null ? cell.toString() : "";
		return text.trim();
	}

	/** Concatenate all cell strings */
	static String rowString(Row row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			sb.append(cellString(row.getCell(i)));
		}
		return sb.toString();
	}

	// ====================== //
	// Location information.
	// ====================== //

	static String atLocaton(Row row) {
		return Messages.format("at-row", row.getSheet().getSheetName(), row.getRowNum() + 1);
	}

	static String atLocaton(Cell cell) {
		return Messages.format("at-cell", cell.getSheet().getSheetName(), cell.getAddress());
	}

	static String atLocaton(Sheet sheet) {
		return Messages.format("at-sheet", sheet.getSheetName());
	}

}
