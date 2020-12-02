package com.jytec.cs.service.api;

public class ClassSearchParams extends SearchParameters {
	public Integer majorId;
	public String name;
	public String degree; // Major#degree
	public Short year; // 入学年
	// public Byte classNo; // 班级号
	// public Integer size;
	// public Integer idc;

	public void setMajorId(Integer majorId) {
		this.majorId = majorId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public void setYear(Short year) {
		this.year = year;
	}

}
