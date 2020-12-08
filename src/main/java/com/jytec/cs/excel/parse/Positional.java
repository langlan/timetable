package com.jytec.cs.excel.parse;

import static com.jytec.cs.excel.parse.Texts.cellString;
import static org.apache.poi.ss.usermodel.CellType.BLANK;

import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public interface Positional {
	/** 0 based */
	int getPosition();

	/** return a Positional witch use a fixed-number (0 based) as position */
	public static Positional fixed(int position) {
		return () -> position;
	}

	/** return a function witch get a cell from row by the {@link #getPosition()} */
	default CellPicker<Cell> asCellPicker() {
		return row -> row.getCell(getPosition());
	}

	/** return a function witch get a cell from row by the {@link #getPosition()} */
	default CellPicker<Cell> asCellPickerWithMerges() {
		return row -> {
			Cell cell = row.getCell(getPosition());
			if (cell == null || cell.getCellType() == BLANK) {
				return MergingAreas.getMergeCell(row.getSheet(), row.getRowNum(), getPosition());
			}
			return cell;
		};
	}

	/**
	 * return a function witch get a cell from row by the {@link #getPosition()}, then convert the cell by converter
	 */
	default <R> CellPicker<R> asCellPicker(Function<Cell, R> converter) {
		return asCellPicker().withConverter(converter);
	}

	/** return a function witch get a cell from row by the {@link #getPosition()}, return cell.toString() */
	default CellPicker<String> asCellStringPicker() {
		return row -> cellString(row.getCell(getPosition()));
	}

	default Function<Sheet, Row> asRowPicker() {
		return it -> it.getRow(getPosition());
	}
}