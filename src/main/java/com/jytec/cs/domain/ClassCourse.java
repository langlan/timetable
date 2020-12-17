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
import com.jytec.cs.domain.Term.TermAware;
import com.jytec.cs.domain.helper.ModelPropAsIdSerializer;

@Table(uniqueConstraints = { //
		@UniqueConstraint(columnNames = { "termId", "the_class_id", "course_code" }) //
})
@Entity
public class ClassCourse extends BaseModel<Long> implements TermAware{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String termId;
	@JsonProperty("classId")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Class theClass;
	@JsonProperty("courseCode")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Course course;
	@JsonProperty("teacherId")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Teacher teacher; // 单教师，或多教师第一个
	private String teacherNames; // 原 excel 教师名，多教师以 '/'分隔

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public Class getTheClass() {
		return theClass;
	}

	public void setTheClass(Class theClass) {
		this.theClass = theClass;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public String getTeacherNames() {
		return teacherNames;
	}

	public void setTeacherNames(String teacherNames) {
		this.teacherNames = teacherNames;
	}
	
	public static ClassCourse of(long classId, String courseCode) {
		ClassCourse ret = new ClassCourse();
		ret.setTheClass(Class.of(classId));
		ret.setCourse(Course.of(courseCode));
		return ret;
	}

}
