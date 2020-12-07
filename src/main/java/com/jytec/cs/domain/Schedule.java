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

@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "the_class_id", "course_code", "termId", "weekno", //
				"dayOfWeek", "timeStart", "timeEnd" }) })
@Entity
public class Schedule extends BaseModel<Long> implements TermAware {
	public static final String TRAININGTYPE_NON = "N";
	public static final String TRAININGTYPE_SCHOOL = "S";
	public static final String TRAININGTYPE_ENTERPRISE = "E";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
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
	private Teacher teacher;
	@JsonProperty("siteId")
	@JsonSerialize(using = ModelPropAsIdSerializer.class)
	@ManyToOne(fetch = FetchType.LAZY)
	private Site site; // 上课地点
	// 时间
	private String termId;
	private byte weekno, dayOfWeek;
	private String date; // format: ref Date.date
	private byte timeStart, timeEnd; // 起止课时
	// Other
	private String trainingType; // N/S/E for 非实训|校内实训|企业实训

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public byte getWeekno() {
		return weekno;
	}

	public void setWeekno(byte weekno) {
		this.weekno = weekno;
	}

	public byte getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(byte dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public byte getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(byte timeStart) {
		this.timeStart = timeStart;
	}

	public byte getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(byte timeEnd) {
		this.timeEnd = timeEnd;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public String getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(String trainingType) {
		this.trainingType = trainingType;
	}

}
