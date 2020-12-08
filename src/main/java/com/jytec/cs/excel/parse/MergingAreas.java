package com.jytec.cs.excel.parse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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

	/** Copy EXACTLY from {@link SheetUtil#getCell(Sheet, int, int)} */
	static Cell getCell(Sheet sheet, int rowIx, int colIx) {
		Row r = sheet.getRow(rowIx);
		if (r != null) {
			return r.getCell(colIx);
		}
		return null;
	}

	/**
	 * copy and refine from {@link SheetUtil#getCellWithMerges(Sheet, int, int)}
	 */
	static Cell getCellWithMerges(Sheet sheet, int rowIdx, int colIdx) {
		final Cell cell = getCell(sheet, rowIdx, colIdx);
		if (cell == null || cell.getCellType() == CellType.BLANK) {
			Cell mc = getMergeCell(sheet, rowIdx, colIdx);
			return mc;
		}
		return cell;
	}

	/**
	 * try get a cell from merged-regions. return null if the given coordinates not in any merged-region.
	 * 
	 * @return return a merged-cell if the given coordinates in any range of merged-regions. or null.
	 * 
	 * @implNote extract from {@link SheetUtil#getCellWithMerges(Sheet, int, int)}
	 */
	static Cell getMergeCell(Sheet sheet, int rowIdx, int colIdx) {
		for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
			if (mergedRegion.isInRange(rowIdx, colIdx)) {
				// The cell wanted is in this merged range
				// Return the primary (top-left) cell for the range
				Row r = sheet.getRow(mergedRegion.getFirstRow());
				if (r != null) {
					return r.getCell(mergedRegion.getFirstColumn());
				}
			}
		}
		return null;
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
