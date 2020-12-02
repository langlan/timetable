package com.jytec.cs.service.api;

public class DateSearchParams extends WeekSearchParams {
	public Boolean holiday;
	public Byte dayOfWeek;
	public Short year;
	public Byte month;

	public void setHoliday(Boolean holiday) {
		this.holiday = holiday;
	}

	public void setDayOfWeek(Byte dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public void setYear(Short year) {
		this.year = year;
	}

	public void setMonth(Byte month) {
		this.month = month;
	}

}
