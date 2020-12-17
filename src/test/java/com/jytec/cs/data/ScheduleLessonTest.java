package com.jytec.cs.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import com.jytec.cs.domain.schedule.ScheduleLesson;

public class ScheduleLessonTest {
	@Test
	public void testLessons() {
		assertArrayEquals(new byte[] { 1, 2 }, ScheduleLesson.lessons((byte) 1, (byte) 4));
		assertArrayEquals(new byte[] { 1, 2, 3 }, ScheduleLesson.lessons((byte) 1, (byte) 6));
		assertArrayEquals(new byte[] { 1, 2, 3, 4 }, ScheduleLesson.lessons((byte) 1, (byte) 8));
		assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, ScheduleLesson.lessons((byte) 1, (byte) 10));
		assertArrayEquals(new byte[] { 2 }, ScheduleLesson.lessons((byte) 3, (byte) 4));
		assertArrayEquals(new byte[] { 2, 3 }, ScheduleLesson.lessons((byte) 3, (byte) 6));
		assertArrayEquals(new byte[] { 2, 3, 4 }, ScheduleLesson.lessons((byte) 3, (byte) 8));
		assertArrayEquals(new byte[] { 2, 3, 4, 5 }, ScheduleLesson.lessons((byte) 3, (byte) 10));
		assertArrayEquals(new byte[] { 3 }, ScheduleLesson.lessons((byte) 5, (byte) 6));
		assertArrayEquals(new byte[] { 3, 4 }, ScheduleLesson.lessons((byte) 5, (byte) 8));
		assertArrayEquals(new byte[] { 3, 4, 5 }, ScheduleLesson.lessons((byte) 5, (byte) 10));
		assertArrayEquals(new byte[] { 4 }, ScheduleLesson.lessons((byte) 7, (byte) 8));
		assertArrayEquals(new byte[] { 4, 5 }, ScheduleLesson.lessons((byte) 7, (byte) 10));
		assertArrayEquals(new byte[] { 5 }, ScheduleLesson.lessons((byte) 9, (byte) 10));
	}
}
