package com.jytec.cs.service.api;

public class CourseSearchParams extends SearchParameters {
	public String code;
	public String name; // 课程名
	public String cate; // 课程性质：公共基础|专业技能|专业技能（选）|实习环节|无
	public String style;// 课程类别：必修课|选修课|限定选修课|无
	public String examineMethod; // 考核方式：考试|考查|无
	public Boolean labByTheory;
	public String locationType;

	public void setCode(String code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCate(String cate) {
		this.cate = cate;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setExamineMethod(String examineMethod) {
		this.examineMethod = examineMethod;
	}

	public void setLabByTheory(Boolean labByTheory) {
		this.labByTheory = labByTheory;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

}
