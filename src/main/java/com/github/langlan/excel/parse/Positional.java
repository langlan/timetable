package com.github.langlan.excel.parse;

import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public interface Positional {
	/** 0 based */
	int getPosition();

	/** set a fixed-number (0 based) for the expecting row. */
	public static Positional fixed(int postion) {
		return () -> postion;
	}

	/** return a function witch get a cell from row by the {@link #getPosition()} */
	default CellPicker<Cell> asCellPicker() {
		return row -> row.getCell(getPosition());
	}

	/**
	 * return a function witch get a cell from row by the {@link #getPosition()}, then convert the cell by converter
	 */
	default <R> CellPicker<R> asCellPicker(Function<Cell, R> converter) {
		return row -> converter.apply(row.getCell(getPosition()));
	}

	/** return a function witch get a cell from row by the {@link #getPosition()}, return cell.toString() */
	default CellPicker<String> asCellStringPicker() {
		return row -> row.getCell(getPosition()).toString();
	}

	default Function<Sheet, Row> asRowPicker() {
		return it -> it.getRow(getPosition());
	}
}