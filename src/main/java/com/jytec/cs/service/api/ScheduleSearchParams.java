package com.jytec.cs.service.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ScheduleSearchParams extends TermSearchParams {
	// TODO: public Range<Byte> weekno, dayOfWeek;
	// TODO: public Range<String> date;
	public Byte weekno, dayOfWeek;
	public String date;
	public Byte timeStart, timeEnd;

	public Long classId, teacherId;
	public String courseCode;
	public Integer siteId, majorId, deptId;
	public String trainingType;

	// special
	public String yearMonth;
	public byte lesson; //for test if lesson*2 between timeStart And End.

	// the-class-join
	public Short classYear;

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

	public void setWeekno(Byte weekno) {
		this.weekno = weekno;
	}

	public void setDayOfWeek(Byte dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setTimeStart(Byte timeStart) {
		this.timeStart = timeStart;
	}

	public void setTimeEnd(Byte timeEnd) {
		this.timeEnd = timeEnd;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	public void setMajorId(Integer majorId) {
		this.majorId = majorId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	public void setTrainingType(String trainingType) {
		this.trainingType = trainingType;
	}

	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
	}
	
	public void setLesson(byte lesson) {
		this.lesson = (byte) (lesson*2);
	}

	public void setClassYear(Short classYear) {
		this.classYear = classYear;
	}
	
	
}
