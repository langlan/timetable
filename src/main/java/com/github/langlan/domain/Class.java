package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { //
		@UniqueConstraint(columnNames = { "major_id", "year", "classNo" }) //
})
public class Class {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@ManyToOne
	private Major major;
	private String name;
	private String degree; // Major#degree
	private short year; // 入学年
	private byte classNo; // 班级号
	private int size; // 学生人数

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Major getMajor() {
		return major;
	}

	public void setMajor(Major major) {
		this.major = major;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public int getYear() {
		return year;
	}

	public byte getClassNo() {
		return classNo;
	}

	public void setClassNo(byte no) {
		this.classNo = no;
	}

	public void setYear(short year) {
		this.year = year;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
