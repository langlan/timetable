package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { //
		@UniqueConstraint(columnNames = { "classId", "no"}), //
})
public class WeekSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private long classId;
	private int termId;
	private byte no;
	private long monday; // day-schedule id
	private long tuesday; // day-schedule id
	private long wednesday; // day-schedule id
	private long thursday; // day-schedule id
	private long friday; // day-schedule id
	private long saturday; // day-schedule id

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public long getClassId() {
		return classId;
	}

	public void setClassId(long classId) {
		this.classId = classId;
	}

	public byte getNo() {
		return no;
	}

	public void setNo(byte no) {
		this.no = no;
	}

	public long getMonday() {
		return monday;
	}

	public void setMonday(long monday) {
		this.monday = monday;
	}

	public long getTuesday() {
		return tuesday;
	}

	public void setTuesday(long tuesday) {
		this.tuesday = tuesday;
	}

	public long getWednesday() {
		return wednesday;
	}

	public void setWednesday(long wednesday) {
		this.wednesday = wednesday;
	}

	public long getThursday() {
		return thursday;
	}

	public void setThursday(long thursday) {
		this.thursday = thursday;
	}

	public long getFriday() {
		return friday;
	}

	public void setFriday(long friday) {
		this.friday = friday;
	}

	public long getSaturday() {
		return saturday;
	}

	public void setSaturday(long saturday) {
		this.saturday = saturday;
	}

}
