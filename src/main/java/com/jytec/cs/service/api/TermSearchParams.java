package com.jytec.cs.service.api;

import com.jytec.cs.domain.Term;

public class TermSearchParams extends SearchParameters {
	public String termId;
	public Short termYear;
	public Byte termMonth;

	/** 202009|2020-09 */
	public void setTerm(String term) {
		setTermId(Term.of(term).getId());
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public void setTermYear(Short termYear) {
		this.termYear = termYear;
	}

	public void setTermMonth(Byte termMonth) {
		this.termMonth = termMonth;
	}
	
}
