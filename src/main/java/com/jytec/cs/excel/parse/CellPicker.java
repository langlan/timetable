package com.jytec.cs.excel.parse;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Row;

/**
 * pick a cell from a row, and convert the cell to a value of type T.
 * 
 * @param T: the value Type. simply it will be Cell, but with a converter, it will be another Type.
 */
interface CellPicker<T> extends Function<Row, T> {
	default <CONTEXT> BiConsumer<Row, CONTEXT> link(BiConsumer<T, CONTEXT> userDefined) {
		// apply(row) : pick a cell from the row
		// accept(picked-value, context) : consume the value with context.
		return (row, context) -> userDefined.accept(apply(row), context);
	}

	default <R> CellPicker<R> withConverter(Function<T, R> converter) {
		// return andThen(converter);
		return (row) -> converter.apply(apply(row));
	}

}