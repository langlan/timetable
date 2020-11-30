package com.jytec.cs.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(uniqueConstraints = {
		// @UniqueConstraint(columnNames = { "the_class_id", "course_id", "termYear", "termMonth", "weekno",
		// "dayOfWeek", "timeStart", "timeEnd" })
})
@Entity
public class Schedule {
	public static final String TRAININGTYPE_NON = "N";
	public static final String TRAININGTYPE_SCHOOL = "S";
	public static final String TRAININGTYPE_ENTERPRISE = "E";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@ManyToOne
	private Class theClass;
	@ManyToOne
	private Course course;
	@ManyToOne
	private Teacher teacher;
	@ManyToOne
	private Site site; // 上课地点
	// 时间
	private short termYear;
	private byte termMonth;
	private byte weekno, dayOfWeek;
	private String date; // format: ref Date.date
	private byte timeStart, timeEnd; // 起止课时
	// Other
	private String trainingType; // N/S/E for 非实训|校内实训|企业实训

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Class getTheClass() {
		return theClass;
	}

	public void setTheClass(Class theClass) {
		this.theClass = theClass;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
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

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public String getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(String trainingType) {
		this.trainingType = trainingType;
	}

}
