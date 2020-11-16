package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ClassCourse {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private long classId;
	private short termYear;
	private byte termMonth;
	private String courseId; // 课程名 name&id
	private long teacherId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getClassId() {
		return classId;
	}

	public void setClassId(long classId) {
		this.classId = classId;
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

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(long teacherId) {
		this.teacherId = teacherId;
	}

}
