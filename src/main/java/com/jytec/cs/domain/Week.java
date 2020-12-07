package com.jytec.cs.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.jytec.cs.domain.Term.TermAware;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "termId", "weekno" }))
public class Week extends BaseModel<String> implements TermAware{
	private String termId;
	private byte weekno;
	@Id
	private String firstDay; // format: ref Date.date
	private String lastDay; // format: ref Date.date

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

	public String getFirstDay() {
		return firstDay;
	}

	public void setFirstDay(String firstDay) {
		this.firstDay = firstDay;
	}

	public String getLastDay() {
		return lastDay;
	}

	public void setLastDay(String lastDay) {
		this.lastDay = lastDay;
	}

}
