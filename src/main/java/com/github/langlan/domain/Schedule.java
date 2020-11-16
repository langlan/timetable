package com.github.langlan.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
public class Schedule {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private long classCourseId;
	// 时间
	private byte weekno;
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

	public long getClassCourseId() {
		return classCourseId;
	}

	public void setClassCourseId(long classCourseId) {
		this.classCourseId = classCourseId;
	}

	public byte getWeekno() {
		return weekno;
	}

	public void setWeekno(byte weekno) {
		this.weekno = weekno;
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
