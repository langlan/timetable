package com.jytec.cs.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ClassCourse extends BaseModel<Long>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private short termYear;
	private byte termMonth;
	@ManyToOne
	private Class theClass;
	@ManyToOne
	private Course course;
	@ManyToOne
	private Teacher teacher; // 单教师，或多教师第一个
	private String teacherNames; // 原 excel 教师名，多教师以 '/'分隔

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getTeacherNames() {
		return teacherNames;
	}

	public void setTeacherNames(String teacherNames) {
		this.teacherNames = teacherNames;
	}

}
