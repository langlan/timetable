package com.jytec.cs.excel.parse;

import java.util.Arrays;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class HeaderRowNotFountException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;
	private Row mostLikeRow;
	private String[] unmatchedHeaderPatterns;

	public HeaderRowNotFountException(Sheet sheet) {
		super(Messages.format("header-row-not-found", sheet.getSheetName()));
	}

	public HeaderRowNotFountException(Row mostLikeRow, String... unmatchedPatterns) {
		super(buildMsg(mostLikeRow, unmatchedPatterns));
		this.mostLikeRow = mostLikeRow;
		this.unmatchedHeaderPatterns = unmatchedPatterns;
	}

	private static String buildMsg(Row mostLikeRow, String[] unmatchedPatterns) {
		String patterns = Strings.join(Arrays.asList(unmatchedPatterns), ',') + "]";
		String msg = Messages.format("header-row-most-like", patterns);
		return msg + Texts.atLocaton(mostLikeRow);
	}

	public Row getMostLikeRow() {
		return mostLikeRow;
	}

	public String[] getUnmatchedHeaderPatterns() {
		return unmatchedHeaderPatterns;
	}
}
