package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { //
		@UniqueConstraint(columnNames = { "major", "year", "no" }) //
})
public class Class {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String major;
	private int year; // 入学年
	private short no; // 班级号 
	private String degree; // 高职/三二 , 也许放在专业里理好一些？
	private int size; // 学生人数
	// instructor

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public short getNo() {
		return no;
	}

	public void setNo(short no) {
		this.no = no;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
