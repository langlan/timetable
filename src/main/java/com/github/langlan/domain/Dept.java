package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Dept { // 系别
	@Id
	private String dept;

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

}
