package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Course {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name; // 课程名
	boolean trainning; // 是否是实训课
	private byte cate; // 课程性质：公共基础|专业技能|专业技能（选）|实习环节|无
	private byte style;// 课程类别：必修课|选修课|限定选修课|无
	private byte checkStyle; // 考核方式：考试|考查|无

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isTrainning() {
		return trainning;
	}

	public void setTrainning(boolean trainning) {
		this.trainning = trainning;
	}

	public byte getCate() {
		return cate;
	}

	public void setCate(byte cate) {
		this.cate = cate;
	}

	public byte getStyle() {
		return style;
	}

	public void setStyle(byte style) {
		this.style = style;
	}

	public byte getCheckStyle() {
		return checkStyle;
	}

	public void setCheckStyle(byte checkStyle) {
		this.checkStyle = checkStyle;
	}

}
