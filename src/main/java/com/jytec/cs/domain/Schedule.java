package com.jytec.cs.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jytec.cs.domain.Term.TermAware;
import com.jytec.cs.domain.helper.ModelPropAsIdSerializer;
import com.jytec.cs.domain.schedule.ScheduleLesson;

@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "course_code", "site_id", //
		"termId", "weekno", "dayOfWeek", "timeStart", "timeEnd" }) })
@Entity
public class Schedule extends BaseModel<Long> implements TermAware {
	public static final String COURSE_TYPE_NORMAL = "N";
	public static final String COURSE_TYPE_TRAINING = "T";
	public static final String TRAININGTYPE_SCHOOL = "S";
	public static final String TRAININGTYPE_ENTERPRISE = "E";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@JsonProperty("courseCode")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Course course;
	@JsonProperty("siteId")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Site site; // 上课地点
	// 时间
	private String termId;
	private byte weekno, dayOfWeek;
	private String date; // format: ref Date.date
	private byte timeStart, timeEnd; // 起止课时
	private byte timeSpan; // timeEnd - timeStart + 1
	private byte lessonSpan; // (timeEnd - timeStart + 1) / 2
	// Other
	private String courseType; // N/T for 普通|实训
	private String trainingType; // S/E for 校内实训|企业实训

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "schedule_teacher")
	List<Teacher> teachers;
	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "schedule_class")
	List<Class> classes;
	private byte classCount = 1, teacherCount = 1;
	@JsonRawValue
	private String teacherIds;
	@JsonRawValue
	private String classIds;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public byte getTimeSpan() {
		if (timeSpan == 0) {
			return (byte) (timeEnd - timeStart + 1);
		}
		return timeSpan;
	}

	public void setTimeSpan(byte timeSpan) {
		this.timeSpan = timeSpan;
	}

	public byte getLessonSpan() {
		if (lessonSpan == 0) {
			return (byte) Math.ceil((timeEnd - timeStart + 1) / 2.0);
		}
		return lessonSpan;
	}

	public void setLessonSpan(byte lessonSpan) {
		this.lessonSpan = lessonSpan;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public String getCourseType() {
		return courseType;
	}

	public void setCourseType(String courseType) {
		this.courseType = courseType;
	}

	public String getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(String trainingType) {
		this.trainingType = trainingType;
	}

	public void recalcRedundant() {
		setTimeSpan((byte) (timeEnd - timeStart + 1));
		setLessonSpan((byte) Math.ceil((timeEnd - timeStart + 1) / 2.0));
		String tids = teachers.stream().map(Teacher::getId).map(Object::toString).collect(Collectors.joining(","));
		String cids = classes.stream().map(Class::getId).map(Object::toString).collect(Collectors.joining(","));
		teacherIds = "[" + tids + "]";
		classIds = "[" + cids + "]";
	}

	public List<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Teacher> teachers) {
		this.teachers = teachers;
	}

	public List<Class> getClasses() {
		return classes;
	}

	public void setClasses(List<Class> classes) {
		this.classes = classes;
	}

	public byte getClassCount() {
		return classCount;
	}

	public void setClassCount(byte classCount) {
		this.classCount = classCount;
	}

	public byte getTeacherCount() {
		return teacherCount;
	}

	public void setTeacherCount(byte teacherCount) {
		this.teacherCount = teacherCount;
	}

	public String getTeacherIds() {
		return teacherIds;
	}

	public void setTeacherIds(String teacherIds) {
		this.teacherIds = teacherIds;
	}

	public String getClassIds() {
		return classIds;
	}

	public void setClassIds(String classIds) {
		this.classIds = classIds;
	}
	
	@Transient
	@JsonProperty("lessons")
	@JsonRawValue
	public String getLessons() {
		byte[] lessons = ScheduleLesson.lessons(timeStart, timeEnd);
		return Arrays.toString(lessons);
	}
}
