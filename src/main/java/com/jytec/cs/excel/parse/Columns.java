package com.jytec.cs.excel.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class Columns<T> {
	static final Function<Cell, String> STRING_CONVERTER = Texts::cellString;
	private List<Col<?, T>> cols = new ArrayList<>();

	/**
	 * scan and find the first matched header-row by using defined cols.
	 * 
	 * @param b
	 */
	public Row findHeaderRow(Sheet sheet, int rowIndexStart, int rowIndexEnd) throws HeaderRowNotFountException {
		List<Col<?, T>> mostLike = null;
		Row mostLikeRow = null;
		int requiredColsCount = countRequiredCols();
		for (int rowIndex = rowIndexStart; rowIndex < rowIndexEnd && rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row != null) {
				List<Col<?, T>> aa = findUnmatchedCols(row);
				if (aa.isEmpty()) {
					return row;
				}
				int matchedColsCount = requiredColsCount - aa.size();
				if (matchedColsCount > 0 && (mostLike == null || mostLike.size() > aa.size())) {
					mostLike = aa;
					mostLikeRow = row;
				}
			}
		}
		if (mostLikeRow != null) {
			String[] unmatchedHeaderPatterns = mostLike.stream().map(Col::getHeaderPattern).toArray(i -> new String[i]);
			throw new HeaderRowNotFountException(mostLikeRow, unmatchedHeaderPatterns);
		}
		throw new HeaderRowNotFountException(sheet);
	}

	/** return a list of required cols which not matched any column in row. */
	public List<Col<?, T>> findUnmatchedCols(Row row) {
		return cols.stream() //
				.filter(col -> !col.isOptional()) //
				.filter(col -> col.matches(row) == null) //
				.collect(Collectors.toList());
	}

	public int countRequiredCols() {
		int optionalCount = (int) cols.stream().filter(Col::isOptional).count();
		return cols.size() - optionalCount;
	}

	public BiConsumer<Row, T> buildRowProcessorByHeaderRow(Row titleRow) {
		BiConsumer<Row, T> consumer = null;
		for (Col<?, T> col : cols) {
			int colIndex = col.indexOfFirst(titleRow);
			if (colIndex > -1) {
				BiConsumer<Row, T> c = col.buildRowProcessor(colIndex);
				consumer = consumer == null ? c : consumer.andThen(c);
			}
		}
		return consumer;
	}

	public Col<Cell, T> col(String headerPattern, BiConsumer<Cell, T> cellConsumer) {
		Col<Cell, T> col = Col.create(headerPattern, cellConsumer);
		cols.add(col);
		return col;
	}

	public <V> Col<V, T> col(String headerPattern, Function<Cell, V> converter, BiConsumer<V, T> cellConsumer) {
		Col<V, T> col = Col.create(headerPattern, converter, cellConsumer);
		cols.add(col);
		return col;
	}

	public Col<String, T> scol(String headerPattern, BiConsumer<String, T> cellStrConsumer) {
		Col<String, T> col = Col.create(headerPattern, STRING_CONVERTER, cellStrConsumer);
		cols.add(col);
		return col;
	}

}