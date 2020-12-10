package com.jytec.cs.excel.parse;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Col<V, CONTEXT> {
	private boolean optional, withMerges;
	private String headerPattern;
	private Function<Cell, V> converter; // convert cell to V value before it being consuming with context.
	private BiConsumer<V, CONTEXT> cellConsumer;

	// V can only be Cell
	private Col(String headerPatter, BiConsumer<V, CONTEXT> cellConsumer) {
		this.cellConsumer = cellConsumer;
		this.headerPattern = headerPatter;
	}

	private Col(String headerPatter, Function<Cell, V> cellConverter, BiConsumer<V, CONTEXT> cellConsumer) {
		this.cellConsumer = cellConsumer;
		this.headerPattern = headerPatter;
		this.converter = cellConverter;
	}

	public Col<V, CONTEXT> optional() {
		this.optional = true;
		return this;
	}

	public Col<V, CONTEXT> withMerges() {
		this.withMerges = true;
		return this;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean isWithMerges() {
		return withMerges;
	}

	public String getHeaderPattern() {
		return headerPattern;
	}

	// in this case, V is Cell
	static <CONTEXT> Col<Cell, CONTEXT> create(String headerPatter, BiConsumer<Cell, CONTEXT> cellConsumer) {
		return new Col<Cell, CONTEXT>(headerPatter, cellConsumer);
	}

	static <V, CONTEXT> Col<V, CONTEXT> create(String headerPatter, Function<Cell, V> converter,
			BiConsumer<V, CONTEXT> cellConsumer) {
		return new Col<V, CONTEXT>(headerPatter, converter, cellConsumer);
	}

	/**
	 * invoke to generate a consumer-with-context.
	 * 
	 * @param colNum the col-index (0-based)
	 * @return a positioned-cell-consumer witch pick a cell by the col-index, convert, consume the converted value with
	 *         context.
	 */
	BiConsumer<Row, CONTEXT> buildRowProcessor(int colNum) {
		Positional pos = Positional.fixed(colNum);
		CellPicker<Cell> cellPicker = (withMerges ? pos.asCellPickerWithMerges() : pos.asCellPicker());
		if (converter != null) {
			return cellPicker.withConverter(converter).link(cellConsumer);
		} else {
			@SuppressWarnings("unchecked")
			BiConsumer<Cell, CONTEXT> _c = (BiConsumer<Cell, CONTEXT>) cellConsumer;
			return cellPicker.link(_c);
		}
	}

	/** find the first matched cell, return its index (0 based). */
	int indexOfFirst(Row headerRow) {
		Cell cell = matches(headerRow);
		if (cell != null) {
			return cell.getColumnIndex();
		} else if (!this.optional) {
			throw new HeaderRowNotFountException("Could not find the column：" + headerPattern, headerRow.getSheet());
		}
		return -1;
	}

	public Cell matches(Row headerRow) {
		if (headerRow == null) {
			return null;
		}
		for (int colIdx = 0; colIdx < headerRow.getLastCellNum(); colIdx++) {
			Cell cell = headerRow.getCell(colIdx);
			String header = cell == null ? null : cell.toString().trim();
			if (this.matchesHeader(header)) {
				return cell;
			}
		}
		return null;
	}

	protected boolean matchesHeader(String colHeader) {
		return colHeader != null && colHeader.matches(headerPattern);
	}
}
