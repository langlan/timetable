package com.jytec.cs.domain;

import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.util.Assert;

import com.jytec.cs.excel.parse.Regex;

@Entity
public class Term extends BaseModel<String> {
	public static final short TERM_YEAR_MIN_VALUE = 2000;
	public static final short TERM_YEAR_MAX_VALUE = 2100;
	public static final byte TERM_MONTH_AUTUMN = 9;
	public static final byte TERM_MONTH_SPRING = 3;
	public static final String NON_VALID_YEAR = "Year out of range：(" + TERM_YEAR_MAX_VALUE + ", " + TERM_YEAR_MIN_VALUE
			+ ")";
	public static final byte COUNT_OF_WEEK_MIN = 10;
	public static final byte COUNT_OF_WEEK_MAX = 25;

	private short termYear;
	private byte termMonth;
	private String firstDay, lastDay;
	private byte countOfWeeks;

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

	public String getFirstDay() {
		return firstDay;
	}

	public void setFirstDay(String firstDay) {
		this.firstDay = firstDay;
	}

	public String getLastDay() {
		return lastDay;
	}

	public void setLastDay(String lastDay) {
		this.lastDay = lastDay;
	}

	public byte getCountOfWeeks() {
		return countOfWeeks;
	}

	public void setCountOfWeeks(byte countOfWeeks) {
		this.countOfWeeks = countOfWeeks;
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

	/** mark */
	public static interface TermAware {
		default void setTerm(Term term) {
			setTermId(term.getId());
		}

		void setTermId(String termId);
	}

	static final Pattern YEAR_MONTH = Pattern.compile("(\\d{4})[^\\d]*(\\d+)");
	
	public static Term of(String termId) {
		String[] groups = Regex.groups(YEAR_MONTH, termId);
		if (groups != null) {
			Term ret = new Term();
			short year = Short.parseShort(groups[1]);
			byte month = Byte.parseByte(groups[2]);
			// ret.setId(year + "0" + month);
			ret.setTermYear(year);
			ret.setTermMonth(month);
			return ret;
		}
		return null;
	}
}
