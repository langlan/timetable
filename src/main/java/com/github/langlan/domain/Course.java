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
	private long classId;
	private String course; // 课程名
	private long teacherId;
	boolean trainning; // 是否是实训课
	private String deafultRoom;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getClassId() {
		return classId;
	}

	public void setClassId(long classId) {
		this.classId = classId;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(long teacherId) {
		this.teacherId = teacherId;
	}

	public boolean isTrainning() {
		return trainning;
	}

	public void setTrainning(boolean trainning) {
		this.trainning = trainning;
	}

	public String getDeafultRoom() {
		return deafultRoom;
	}

	public void setDeafultRoom(String deafultRoom) {
		this.deafultRoom = deafultRoom;
	}
}
