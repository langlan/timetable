package com.jytec.cs.service.api;

public class MajorSearchParams extends SearchParameters{
	public String degree;
	public Integer deptId;

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}
}
