package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Major { // 专业
	@Id
	String major;
	String dept;

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

}
