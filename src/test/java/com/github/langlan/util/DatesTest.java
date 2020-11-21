package com.github.langlan.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.langlan.util.Dates.CalenderWrapper;

public class DatesTest {
	@Test
	public void testWeekDaySetting() {
		// test day setting and reading: NOTE: the last day of 2020.11 is 2020.11.30
		CalenderWrapper c = Dates.wrapper();
		assertArrayEquals(new int[] { 2020, 11, 21 }, c.go(2020, 11, 21).getTimesYMD());
		assertArrayEquals(new int[] { 2020, 12, 1 }, c.go(2020, 11, 31).getTimesYMD());

		// test week functions:

		int[] date = new int[] { 2020, 11, 21 }; // NOTE: 2020.12.21 is Saturday.
		// Monday-first: so from Saturday to Sunday will forward 1 day
		assertArrayEquals(new int[] { 2020, 11, 22 }, c.go(date).letMondayFirst().goSunday().getTimesYMD());
		// Sunday-first: so from Saturday to Sunday will backward 6 days
		assertArrayEquals(new int[] { 2020, 11, 15 }, c.go(date).letSundayFirst().goSunday().getTimesYMD());

		date = new int[] { 2020, 12, 5 }; // another Saturday.
		// forward 1 day
		assertArrayEquals(new int[] { 2020, 12, 6 }, c.go(date).letMondayFirst().goSunday().getTimesYMD());
		// backward 6 days [keeping in same week, but same month is not necessary]
		assertArrayEquals(new int[] { 2020, 11, 29 }, c.go(date).letSundayFirst().goSunday().getTimesYMD());
		
		assertEquals(6, c.go(date).letMondayFirst().getDayOfWeek());
		assertEquals(7, c.go(date).letSundayFirst().getDayOfWeek());
	}

	@Test
	public void testNegativeIntegerMod() {
		assertEquals(0, 7 % 7);
		assertEquals(1, 1 % 7);
		assertEquals(-1, -1 % 7); // not 6
	}
}
