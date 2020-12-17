package com.jytec.cs.domain.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(ScheduleLesson.class)
public class ScheduleLesson implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private byte timeStart;
	@Id
	private byte timeEnd;
	@Id
	private byte lesson;

	public byte getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(byte timeStart) {
		this.timeStart = timeStart;
	}

	public byte getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(byte timeEnd) {
		this.timeEnd = timeEnd;
	}

	public byte getLesson() {
		return lesson;
	}

	public void setLesson(byte lesson) {
		this.lesson = lesson;
	}

	public static ScheduleLesson of(byte timeStart, byte timeEnd, byte lesson) {
		ScheduleLesson ret = new ScheduleLesson();
		ret.setTimeStart(timeStart);
		ret.setTimeEnd(timeEnd);
		ret.setLesson(lesson);
		return ret;
	}

	public static List<ScheduleLesson> of(byte timeStart, byte timeEnd) {
		List<ScheduleLesson> ret = new ArrayList<>((int) Math.ceil((timeEnd - timeStart + 1) / 2.0));
		for (byte lesson : lessons(timeStart, timeEnd)) {
			ret.add(of(timeStart, timeEnd, lesson));
		}
		return ret;
	}

	public static byte[] lessons(byte timeStart, byte timeEnd) {
		if (timeStart % 2 == 0 || (timeEnd - timeStart) % 2 == 0) {
			throw new IllegalArgumentException("Illegal time-range: " + timeStart + ", " + timeEnd);
		}
		byte[] ret = new byte[(int) Math.ceil((timeEnd - timeStart + 1) / 2.0)];
		byte firstLesson = (byte) Math.ceil(timeStart / 2.0);
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (byte) (firstLesson + i);
		}
		return ret;
	}

}
