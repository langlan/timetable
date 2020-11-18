package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Date { // 教学日期
	@Id
	private String date; // yyyyMMdd
	private byte weekno; // 1 based
	private byte dayOfWeek; // 1based
	private short year;
	private byte month; // 1based
	private boolean holiday;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public byte getWeekno() {
		return weekno;
	}

	public void setWeekno(byte weekno) {
		this.weekno = weekno;
	}

	public byte getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(byte dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public short getYear() {
		return year;
	}

	public void setYear(short year) {
		this.year = year;
	}

	public byte getMonth() {
		return month;
	}

	public void setMonth(byte month) {
		this.month = month;
	}

	public boolean isHoliday() {
		return holiday;
	}

	public void setHoliday(boolean holiday) {
		this.holiday = holiday;
	}

}
