package com.jytec.cs.service.api;

import java.util.regex.Pattern;

import com.jytec.cs.excel.parse.Regex;

public class TermSearchParams extends SearchParameters {
	public Short termYear;
	public Byte termMonth;

	static final Pattern YEAR_MONTH = Pattern.compile("(\\d{4})[^\\d]*(\\d+)");

	/** 202009|2020-09 */
	public void setTerm(String term) {
		String[] groups = Regex.groups(YEAR_MONTH, term);
		if (groups != null) {
			setTermYear(Short.parseShort(groups[1]));
			setTermMonth(Byte.parseByte(groups[2]));
		}
	}

	public void setTermYear(Short termYear) {
		this.termYear = termYear;
	}

	public void setTermMonth(Byte termMonth) {
		this.termMonth = termMonth;
	}
	
}
