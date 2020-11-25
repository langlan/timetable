package com.jytec.cs.excel.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Columns<T> {
	static final Function<Cell, String> STRING_CONVERTER = cell -> cell != null ? cell.toString().trim() : "";
	private List<Col<T, ?>> cols = new ArrayList<>();

	public BiConsumer<Row, T> buildByHeaderRow(Row titleRow) {
		BiConsumer<Row, T> consumer = null;
		for (Col<T, ?> col : cols) {
			int colIndex = col.indexOfFirst(titleRow);
			if (colIndex > -1) {
				BiConsumer<Row, T> c = col.asCellBiConsumer(colIndex);
				consumer = consumer == null ? c : consumer.andThen(c);
			}
		}
		return consumer;
	}

	public Columns<T> col(String headerPattern, BiConsumer<Cell, T> cellConsumer) {
		cols.add(Col.create(headerPattern, cellConsumer));
		return this;
	}

	public Columns<T> colOptional(String headerPattern, BiConsumer<Cell, T> cellConsumer) {
		cols.add(Col.create(headerPattern, cellConsumer).optional());
		return this;
	}

	public <V> Columns<T> col(String headerPattern, Function<Cell, V> converter, BiConsumer<V, T> cellConsumer) {
		cols.add(Col.create(headerPattern, converter, cellConsumer));
		return this;
	}

	public <V> Columns<T> colOptional(String headerPattern, Function<Cell, V> converter, BiConsumer<V, T> cellConsumer) {
		cols.add(Col.create(headerPattern, converter, cellConsumer).optional());
		return this;
	}

	public Columns<T> scol(String headerPattern, BiConsumer<String, T> cellConsumer) {
		cols.add(Col.create(headerPattern, STRING_CONVERTER, cellConsumer));
		return this;
	}

	public Columns<T> scolOptional(String headerPattern, BiConsumer<String, T> cellConsumer) {
		cols.add(Col.create(headerPattern, STRING_CONVERTER, cellConsumer).optional());
		return this;
	}

}