package com.jytec.cs.excel.schedule;

import org.apache.poi.ss.usermodel.Cell;

public class LessonTime {
	final byte start, end;
	public final Cell cell;

	public LessonTime(byte timeStart, byte timeEnd, Cell cell) {
		this.cell = cell;
		this.start = timeStart;
		this.end = timeEnd;
	}

	public boolean overlappedWith(LessonTime other) {
		return (other.start <= start && start <= other.end) || (start <= other.start && other.start <= end);
	}

	public boolean contains(LessonTime other) {
		return start <= other.start && other.end <= end;
	}

	public boolean excludes(LessonTime other) {
		return end < other.start || other.end < start;
	}

	public boolean equals(LessonTime other) {
		return start == other.start && end == other.end;
	}

}