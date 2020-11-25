package com.github.langlan.excel.parse;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Col<CONTEXT, V> {
	private boolean optional;
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

	Col<CONTEXT, V> optional() {
		this.optional = true;
		return this;
	}

	// in this case, V is Cell
	static <CONTEXT> Col<CONTEXT, Cell> create(String headerPatter, BiConsumer<Cell, CONTEXT> cellConsumer) {
		return new Col<CONTEXT, Cell>(headerPatter, cellConsumer);
	}

	static <CONTEXT, V> Col<CONTEXT, V> create(String headerPatter, Function<Cell, V> converter,
			BiConsumer<V, CONTEXT> cellConsumer) {
		return new Col<CONTEXT, V>(headerPatter, converter, cellConsumer);
	}

	/**
	 * invoke to generate a consumer-with-context.
	 * 
	 * @param colNum the col-index (0-based)
	 * @return a positioned-cell-consumer witch pick a cell by the col-index, convert, consume the converted value with
	 *         context.
	 */
	BiConsumer<Row, CONTEXT> asCellBiConsumer(int colNum) {
		if (converter != null) {
			return Positional.fixed(colNum).asCellPicker(converter).asBiConsumer(cellConsumer);
		}
		@SuppressWarnings("unchecked")
		BiConsumer<Cell, CONTEXT> _c = (BiConsumer<Cell, CONTEXT>) cellConsumer;
		return Positional.fixed(colNum).asCellPicker().asBiConsumer(_c);

	}

	/** find the first matched cell, return its index (0 based). */
	int indexOfFirst(Row headerRow) {
		for (int i = 0; i < headerRow.getLastCellNum(); i++) {
			Cell cell = headerRow.getCell(i);
			String header = cell == null ? null : cell.toString().trim();
			if (header == null)
				continue;
			if (header.matches(headerPattern)) {
				return i;
			}
		}
		if (!this.optional) {
			throw new IllegalStateException("Could not find the column：" + headerPattern);
		}
		return -1;
	}

}
