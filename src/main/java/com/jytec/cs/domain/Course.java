package com.jytec.cs.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(uniqueConstraints = { //
		@UniqueConstraint(columnNames = { "code" }) //
})
@Entity
public class Course extends BaseModel<String>{
	@Id
	private String code;
	private String name; // 课程名
	private String cate; // 课程性质：公共基础|专业技能|专业技能（选）|实习环节|无
	private String style;// 课程类别：必修课|选修课|限定选修课|无
	private String examineMethod; // 考核方式：考试|考查|无
	private boolean labByTheory;
	private String locationType;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCate() {
		return cate;
	}

	public void setCate(String cate) {
		this.cate = cate;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getExamineMethod() {
		return examineMethod;
	}

	public void setExamineMethod(String examineMethod) {
		this.examineMethod = examineMethod;
	}

	public boolean isLabByTheory() {
		return labByTheory;
	}

	public void setLabByTheory(boolean labByTheory) {
		this.labByTheory = labByTheory;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

}
