package com.jytec.cs.service.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ScheduleSearchParams {
	public Short termYear;
	public Byte termMonth;
	// TODO: public Range<Byte> weekno, dayOfWeek;
	// TODO: public Range<String> date;
	public Byte weekno, dayOfWeek;
	public String date;
	public Byte timeStart, timeEnd;

	public Long classId, teacherId;
	public String courseId;
	public Integer siteId;

	public String trainingType;
	
	// special
	public String yearMonth;
	
	public String getYearMonth() {
		return yearMonth;
	}

	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
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

	public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

	public Long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	public String getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(String trainingType) {
		this.trainingType = trainingType;
	}

	public Byte getWeekno() {
		return weekno;
	}

	public void setWeekno(Byte weekno) {
		this.weekno = weekno;
	}

	public Byte getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Byte dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setTermYear(Short termYear) {
		this.termYear = termYear;
	}

	public void setTermMonth(Byte termMonth) {
		this.termMonth = termMonth;
	}

	public void setTimeStart(Byte timeStart) {
		this.timeStart = timeStart;
	}

	public void setTimeEnd(Byte timeEnd) {
		this.timeEnd = timeEnd;
	}

	public Map<String, Object> getMapOfNonEmpty() {
		Field[] fields = getClass().getFields();// all public
		Map<String, Object> ret = new HashMap<>();
		for (Field f : fields) {
			try {
				Object value = f.get(this);
				if (value != null) {
					ret.put(f.getName(), value);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException("DEV: not possible!");
			}
		}
		return ret;
	}
}
