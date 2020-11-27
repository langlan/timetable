package com.jytec.cs.excel.parse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.SheetUtil;

public interface MergingAreas {
	static CellRangeAddress getMergingArea(Cell cell) {
		for (CellRangeAddress ra : cell.getSheet().getMergedRegions()) {
			if (ra.getFirstRow() == cell.getRowIndex() && ra.getFirstColumn() == cell.getColumnIndex())
				return ra;
		}
		return null;
	}

	/** copy and refine from {@link SheetUtil#getCellWithMerges(Sheet, int, int)} */
	static Cell getCellWithMerges(Sheet sheet, int rowIx, int colIx) {
		final Cell c = SheetUtil.getCell(sheet, rowIx, colIx);
		String cellString = (c != null ? c.toString() : null);
		if (c != null && cellString != null && !cellString.isEmpty())
			return c;
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
		return c;
	}
	
	static Cell getCell(Sheet sheet, int rowIx, int colIx) {
		return SheetUtil.getCell(sheet, rowIx, colIx);
	}

	// for large files.

	/** cell-address:A1 style. */
	static Map<String, CellRangeAddress> indexedByCellAddress(Sheet sheet) {
		List<CellRangeAddress> mrs = sheet.getMergedRegions();
		Map<String, CellRangeAddress> ret = new HashMap<>();
		for (CellRangeAddress ra : mrs) {
			String key = CellReference.convertNumToColString(ra.getFirstColumn()) + (ra.getFirstRow() + 1);
			ret.put(key, ra);
		}
		return ret;
	}

}
