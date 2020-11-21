package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.util.Assert;

@Entity
public class Term {
	public static final short TERM_YEAR_MIN_VALUE = 2000;
	public static final short TERM_YEAR_MAX_VALUE = 2100;
	public static final String NON_VALID_YEAR = "Year out of range：(" + TERM_YEAR_MAX_VALUE + ", " + TERM_YEAR_MIN_VALUE + ")";
	private short termYear;
	private byte termMonth;

	@Id
	public String getId() {
		if (termMonth > 9)
			throw new IllegalStateException("学期月份有误：" + termMonth);
		return termYear + "0" + termMonth;
	}

	public void setId(String id) {

	}

	public short getTermYear() {
		return termYear;
	}

	public void setTermYear(short termYear) {
		this.termYear = termYear;
	}

	public byte getTermMonth() {
		return termMonth;
	}

	public void setTermMonth(byte termMonth) {
		this.termMonth = termMonth;
	}

	public static Term of(int termYear, int termMonth) {
		Assert.isTrue(termYear >= TERM_YEAR_MIN_VALUE && termYear <= TERM_YEAR_MAX_VALUE, NON_VALID_YEAR);
		Assert.isTrue(termMonth >= 1 && termMonth <=12, "月份超出范围：（1，12）");
		Term term = new Term();
		term.termYear = (short) termYear;
		term.termMonth = (byte) termMonth;
		return term;
	}
}
