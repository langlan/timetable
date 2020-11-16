package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Term {

	@Id
	private String termStart; // yyyyMM 学期开始年月
	private String termEnd; // yyyyMMdd 学期截止年月日

	public String getTermStart() {
		return termStart;
	}

	public void setTermStart(String termStart) {
		this.termStart = termStart;
	}

	public String getTermEnd() {
		return termEnd;
	}

	public void setTermEnd(String termEnd) {
		this.termEnd = termEnd;
	}

}
