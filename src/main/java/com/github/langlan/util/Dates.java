package com.github.langlan.util;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.YEAR;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public interface Dates {

	/** create a instance of {@link CalenderWrapper}, use an existed calendar */
	static CalenderWrapper wrap(Calendar calender) {
		return new CalenderWrapper(calender);
	}

	/** create a instance of {@link CalenderWrapper} */
	static CalenderWrapper wrapper() {
		return new CalenderWrapper(Calendar.getInstance());
	}

	static Date of(int year, int month, int dayOfMonth) {
		return Dates.wrapper().go(year, month, dayOfMonth).getTime();
	}

	/**
	 * Not Thread safe, for the sake of raw calendar.
	 * <p>
	 * utilities for helping with java.util.Date.
	 * <ul>
	 * <li>go/plus/minus: for setting time.</li>
	 * <li>let/use: for configuring the wrapped calendar</li>
	 * <li>with: for configuring the wrapper itself.</li>
	 * <li>get: for reading.</li>
	 * </ul>
	 */
	class CalenderWrapper {
		private final Calendar c;
		private DateFormat formatter;

		private CalenderWrapper(Calendar calender) {
			this.c = calender;
		}

		public CalenderWrapper addDay(int num) {
			c.add(DATE, num);
			return this;
		}

		public CalenderWrapper addWeek(int num) {
			c.add(DATE, num * 7);
			return this;
		}

		public CalenderWrapper addMonth(int num) {
			c.add(MONTH, num);
			return this;
		}

		/** set time: will change the wrapped calender's state. */
		public CalenderWrapper go(Date date) {
			c.setTime(date);
			return this;
		}

		/** parse and go. need {@link #with(DateFormat)} first. see {@link #go(Date)} */
		public CalenderWrapper go(String date) throws ParseException {
			c.setTime(parse(date));
			return this;
		}

		/**
		 * set time: will change the wrapped calender's state.
		 * 
		 * @param year
		 * @param month 1-12 (1 based, 1 for January, different from raw-calendar witch is 0 based)
		 * @param day   dayOfMonth
		 * @return
		 * @see #go(Date)
		 */
		public CalenderWrapper go(int year, int month, int day) {
			c.set(year, month - 1, day);
			return this;
		}

		/** set [year, month(1-12), day, hour, minute, second, millisecond]. extra parameters will be ignored */
		public CalenderWrapper go(int... values) {
			int[] fields = new int[] { YEAR, MONTH, DATE, HOUR, MINUTE, SECOND, MILLISECOND };
			int len = values.length > fields.length ? fields.length : values.length;
			for (int i = 0; i < len; i++) {
				if (fields[i] == MONTH) {
					c.set(MONTH, values[i] - 1); // from 1 based to 0 based.
				} else {
					c.set(fields[i], values[i]);
				}
			}
			return this;
		}

		/**
		 * keep in the same week, respect to {@link #letMondayFirst()}, {@link #letSundayFirst()},
		 * {@link Calendar#setFirstDayOfWeek(int)}
		 */
		public CalenderWrapper goMonday() {
			c.set(Calendar.DAY_OF_WEEK, MONDAY);
			return this;
		}

		/**
		 * keep in the same week, respect to {@link #letMondayFirst()}, {@link #letSundayFirst()},
		 * {@link Calendar#setFirstDayOfWeek(int)}
		 */
		public CalenderWrapper goSunday() {
			c.set(Calendar.DAY_OF_WEEK, SUNDAY);
			return this;
		}

		/**
		 * 1 based. 1 represents the first-day-of-week and 7 for last-day-of-week.
		 * <p>
		 * e.g. <br/>
		 * 1 for Monday and 7 for Sunday if {@link #letMondayFirst()}. or <br/>
		 * 1 for Sunday and 7 for Saturday if {@link #letSundayFirst()}.
		 * 
		 * @param i 1-7
		 * @see #getDayOfWeek()
		 */
		public CalenderWrapper goDayOfWeek(int i) {
			if (i < 1 || i > 7) {
				throw new IllegalArgumentException("invalid value, should be 1~7 but was:" + i);
			}
			int currentDayOfWeek = getDayOfWeek();
			if (currentDayOfWeek != i) {
				addDay(i - currentDayOfWeek);
			}
			return this;
		}

		// ========= unwrap ========= //

		public Calendar unwrap() {
			return c;
		}

		public Calendar get() {
			return c;
		}

		// === configuration methods === //

		/**
		 * let Monday be the first day of week.
		 * 
		 * @see #letSundayFirst()
		 * @see #goMonday()
		 * @see #goSunday()
		 * @see #goDayOfWeek(int)
		 * @see #getDayOfWeek()
		 */
		public CalenderWrapper letMondayFirst() {
			c.setFirstDayOfWeek(MONDAY);
			return this;
		}

		/**
		 * let Sunday be the first day of week.
		 * 
		 * @see #letSundayFirst()
		 * @see #goMonday()
		 * @see #goSunday()
		 * @see #goDayOfWeek(int)
		 * @see #getDayOfWeek()
		 */
		public CalenderWrapper letSundayFirst() {
			c.setFirstDayOfWeek(SUNDAY);
			return this;
		}

		public CalenderWrapper use(TimeZone timeZone) {
			c.setTimeZone(timeZone);
			return this;
		}

		// see #with for format and parse configuration

		// ========= read methods. ========= //
		public Date getTime() {
			return c.getTime();
		}

		/**
		 * alias for {@code getTimes(3)}. return an array contains year, month (1-12), day.
		 * <p>
		 * e.g. 2000-10-1 -> [2000, 10, 1]
		 */
		public int[] getTimesYMD() {
			return getTimes(3);
		}

		/** alias for {@code getTimes(7)} */
		public int[] getTimes() {
			return getTimes(7);
		}

		/**
		 * get [year, month(1-12), day, hour, minute, second, millisecond].
		 * 
		 * @param arrayLength 8 and 8+ will be treated as 7.
		 * @return an array that its length will be {@code arrayLength}.
		 */
		public int[] getTimes(int arrayLength) {
			int[] fields = new int[] { YEAR, MONTH, DATE, HOUR, MINUTE, SECOND, MILLISECOND };
			int len = arrayLength > fields.length ? fields.length : arrayLength;
			int[] values = new int[len];
			for (int i = 0; i < len; i++) {
				if (fields[i] == MONTH) {
					values[i] = c.get(MONTH) + 1; // from 0 based to 1 based.
				} else {
					values[i] = c.get(fields[i]);
				}
			}
			return values;
		}

		public int getYear() {
			return c.get(YEAR);
		}

		/** 1 based. 1 for January */
		public int getMonth() {
			return c.get(MONTH) + 1;
		}

		public int getDayOfMonth() {
			return c.get(DAY_OF_MONTH);
		}

		/**
		 * 1 based. 1 represents the first-day-of-week and 7 for last-day-of-week.
		 * <p>
		 * e.g. <br/>
		 * 1 for Monday and 7 for Sunday if {@link #letMondayFirst()}. or <br/>
		 * 1 for Sunday and 7 for Saturday if {@link #letSundayFirst()}.
		 * 
		 * @see #letMondayFirst()
		 * @see #letSundayFirst()
		 * @see Calendar#setFirstDayOfWeek(int)
		 */
		public int getDayOfWeek() {
			int firstDayOfWeek = c.getFirstDayOfWeek();
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			int delta = dayOfWeek - firstDayOfWeek;
			return (delta + 7) % 7 + 1;
		}

		/** 1~360+ */
		public int getDayOfYear() {
			return c.get(DAY_OF_YEAR);
		}

		/** for parse and format. see {@link #format()} */
		public CalenderWrapper with(DateFormat format) {
			this.formatter = format;
			return this;
		}

		/** format with formatter. see {@link #with(DateFormat)}, {@link #parse(String)}, */
		public String format() {
			return this.formatter.format(c.getTime());
		}

		/**
		 * parse with formatter. see {@link #with(DateFormat)}.
		 * <p>
		 * will not change the wrapped calendar's state, but {@link #go(String)} will.
		 */
		public Date parse(String date) throws ParseException {
			if (this.formatter == null) {
				throw new NullPointerException("Use #with(DateFormat) to set a formatter first.");
			}
			return this.formatter.parse(date);
		}

		@Override
		public String toString() {
			return Arrays.toString(getTimes());
		}
	}

}
