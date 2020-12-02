package com.jytec.cs.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jytec.cs.domain.helper.ModelPropAsIdSerializer;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "roomType" }))
public class Site extends BaseModel<Integer>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String code; // legacy
	private String name;
	private String shortName;
	private String roomType; // 标准教室|专业机房|...
	private int capacity;
	@JsonProperty("deptId")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Dept dept; // 最终指派
	private String multimedia;
	private String name4Training; // 实训基地名
	private String memo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Dept getDept() {
		return dept;
	}

	public void setDept(Dept dept) {
		this.dept = dept;
	}

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

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String type) {
		this.roomType = type;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getMultimedia() {
		return multimedia;
	}

	public void setMultimedia(String multimedia) {
		this.multimedia = multimedia;
	}

	public String getName4Training() {
		return name4Training;
	}

	public void setName4Training(String name4Training) {
		this.name4Training = name4Training;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
