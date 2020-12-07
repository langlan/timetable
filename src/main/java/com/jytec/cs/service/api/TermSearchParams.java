package com.jytec.cs.service.api;

import java.util.regex.Pattern;

import com.jytec.cs.excel.parse.Regex;

public class TermSearchParams extends SearchParameters {
	public String termId;
	public Short termYear;
	public Byte termMonth;

	static final Pattern YEAR_MONTH = Pattern.compile("(\\d{4})[^\\d]*(\\d+)");

	/** 202009|2020-09 */
	public void setTerm(String term) {
		String[] groups = Regex.groups(YEAR_MONTH, term);
		if (groups != null) {
			short year = Short.parseShort(groups[1]);
			byte month = Byte.parseByte(groups[2]);
			setTermId(year + "0" + month);
		}
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public void setTermYear(Short termYear) {
		this.termYear = termYear;
	}

	public void setTermMonth(Byte termMonth) {
		this.termMonth = termMonth;
	}

}
