package com.github.langlan.excel.parse;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Row;

/** pick a cell from a row */
interface CellPicker<T> extends Function<Row, T> {
	default <CONTEXT> BiConsumer<Row, CONTEXT> asBiConsumer(BiConsumer<T, CONTEXT> userDefined) {
		// apply(row) : pick a cell from the row
		// accept(picked-value, context) : consume the value with context.
		return (row, context) -> userDefined.accept(apply(row), context);
	}
}