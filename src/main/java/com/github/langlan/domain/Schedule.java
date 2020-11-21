package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Schedule {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne
	private ClassCourse classCourse;
	// 时间
	private byte weekno, dayOfWeek;
	private String date;
	private byte timeStart, timeEnd; // 起时课时
	// 地点
	private String room; // name&id

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ClassCourse getClassCourse() {
		return classCourse;
	}

	public void setClassCourse(ClassCourse classCourse) {
		this.classCourse = classCourse;
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

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

}
