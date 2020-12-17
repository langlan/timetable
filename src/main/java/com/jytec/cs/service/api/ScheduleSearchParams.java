package com.jytec.cs.service.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ScheduleSearchParams extends TermSearchParams {
	// TODO: public Range<Byte> weekno, dayOfWeek;
	// TODO: public Range<String> date;
	public Byte weekno, dayOfWeek;
	public String date;
	public Byte timeStart, timeEnd, timeSpan, lessonSpan;;

	public Long classId, teacherId;
	public String courseCode;
	public Integer siteId, majorId, deptId;
	public String courseType, trainingType;
	public String[] courseTypes, trainingTypes;

	// special
	public String yearMonth;
	public Byte lesson; // for test if lesson*2 between timeStart And End.

	// the-class-join
	public Short classYear;
	// course-join
	public String courseCate;
	public Byte teacherCount, classCount;

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

	public void setTimeSpan(Byte timeSpan) {
		this.timeSpan = timeSpan;
	}

	public void setLessonSpan(Byte lessonSpan) {
		this.lessonSpan = lessonSpan;
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

	public void setCourseType(String courseType) {
		if (courseType != null && courseType.contains(",")) {
			this.courseTypes = courseType.split("\\s*,\\s*");
		} else {
			this.courseType = courseType;
		}
	}

	public void setTrainingType(String trainingType) {
		if (trainingType != null && trainingType.contains(",")) {
			this.trainingTypes = trainingType.split("\\s*,\\s*");
		} else {
			this.trainingType = trainingType;
		}
	}

	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
	}

	public void setLesson(Byte lesson) {
		this.lesson = (byte) (lesson * 2);
	}

	public void setClassYear(Short classYear) {
		this.classYear = classYear;
	}

	public void setCourseCate(String courseCate) {
		this.courseCate = courseCate;
	}

	public void setTeacherCount(Byte teacherCount) {
		this.teacherCount = teacherCount;
	}

	public void setClassCount(Byte classCount) {
		this.classCount = classCount;
	}

	public boolean needJoinClasses() {
		return classId != null || classYear != null || majorId != null || deptId != null;
	}

	public boolean needjoinTeachers() {
		return teacherId != null;
	}

}
