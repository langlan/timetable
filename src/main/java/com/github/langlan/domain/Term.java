package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Term {

	private short termYear;
	private byte termMonth;

	@Id
	public String getId() {
		if (termMonth > 9)
			throw new IllegalStateException("学期月份有误：" + termMonth);
		return termYear + "0" + termMonth;
	}

	public void setId(String id) {

	}

	public short getTermYear() {
		return termYear;
	}

	public void setTermYear(short termYear) {
		this.termYear = termYear;
	}

	public byte getTermMonth() {
		return termMonth;
	}

	public void setTermMonth(byte termMonth) {
		this.termMonth = termMonth;
	}

}
