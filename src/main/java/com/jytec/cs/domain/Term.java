package com.jytec.cs.domain;

import java.beans.Transient;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.util.Assert;

@Entity
public class Term {
	public static final short TERM_YEAR_MIN_VALUE = 2000;
	public static final short TERM_YEAR_MAX_VALUE = 2100;
	public static final byte TERM_MONTH_AUTUMN = 9;
	public static final byte TERM_MONTH_SPRING = 3;
	public static final String NON_VALID_YEAR = "Year out of range：(" + TERM_YEAR_MAX_VALUE + ", " + TERM_YEAR_MIN_VALUE
			+ ")";

	private short termYear;
	private byte termMonth;

	@Id
	public String getId() {
		return termYear + "0" + termMonth;
	}

	public void setId(String id) {
	}

	public String getName() {
		String season = termMonth > 6 ? "秋季" : "春季";
		return termYear + "年" + season + "学期";
	}

	public void setName(String name) {
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

	public static Term ofAutumn(int termYear) {
		return of(termYear, TERM_MONTH_AUTUMN);
	}

	public static Term ofSpring(int termYear) {
		return of(termYear, TERM_MONTH_SPRING);
	}

	private static Term of(int termYear, byte termMonth) {
		Assert.isTrue(termYear >= TERM_YEAR_MIN_VALUE && termYear <= TERM_YEAR_MAX_VALUE, NON_VALID_YEAR);
		Term term = new Term();
		term.termYear = (short) termYear;
		term.termMonth = (byte) termMonth;
		return term;
	}

	public static interface TermAware {
		short getTermYear();

		byte getTermMonth();

		void setTermYear(short termYear);

		void setTermMonth(byte termMonth);

		@Transient
		default Term getTerm() {
			return Term.of(getTermYear(), getTermMonth());
		}

		default void setTerm(Term term) {
			setTermYear(term.getTermYear());
			setTermMonth(term.getTermMonth());
		}
	}
}
