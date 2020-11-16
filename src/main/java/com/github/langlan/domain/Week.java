package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Week {
	private short termYear;
	private byte termMonth;
	private byte weekno;
	private String firstDay, lastDay; // yyyyMMdd

	@Id
	public String getId() {
		if (termMonth > 9)
			throw new IllegalStateException("学期月份有误：" + termMonth);
		return termYear + "0" + termMonth + "-" + (weekno > 9 ? (" ") + weekno : weekno);
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

	public byte getWeekno() {
		return weekno;
	}

	public void setWeekno(byte weekno) {
		this.weekno = weekno;
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

}
