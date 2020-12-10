package com.jytec.cs.excel.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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

	/** always return a not null string, trimmed */
	static String cellString(Cell cell) {
		if (cell != null) {
			if (cell.getCellType() == CellType.NUMERIC) {
				double dv = cell.getNumericCellValue();
				long lv = (long) dv;
				if(lv==dv) {
					return Long.toString(lv);
				}
			}
			return cell.toString().trim();
		}
		return "";
	}

	/** Concatenate all cell strings without delimiter, always return a not null string */
	static String rowString(Row row) {
		return rowString(row, null);
	}

	/** Concatenate all cell strings, always return a not null string */
	static String rowString(Row row, String delimiter) {
		if (row == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			if (sb.length() > 0 && delimiter != null) {
				sb.append(delimiter);
			}
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

	static String atLocaton(Cell cell, boolean withSheetInfo) {
		return withSheetInfo ? Messages.format("at-cell", cell.getSheet().getSheetName(), cell.getAddress())
				: Messages.format("at-cell-without-sheet", cell.getAddress());
	}

	static String atLocaton(Sheet sheet) {
		return Messages.format("at-sheet", sheet.getSheetName());
	}

}
