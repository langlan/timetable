package com.github.langlan.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
public class DaySchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private long am12; // courseId
	private long am34; // courseId
	private long pm12; // courseId
	private long pm34; // courseId
	private long pm56; // courseId
	// TODO: room?! always same room for a classed-course ? 

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getAm12() {
		return am12;
	}

	public void setAm12(long am12) {
		this.am12 = am12;
	}

	public long getAm34() {
		return am34;
	}

	public void setAm34(long am34) {
		this.am34 = am34;
	}

	public long getPm12() {
		return pm12;
	}

	public void setPm12(long pm12) {
		this.pm12 = pm12;
	}

	public long getPm34() {
		return pm34;
	}

	public void setPm34(long pm34) {
		this.pm34 = pm34;
	}

	public long getPm56() {
		return pm56;
	}

	public void setPm56(long pm56) {
		this.pm56 = pm56;
	}
	
	
}
