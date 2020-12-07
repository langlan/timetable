package com.jytec.cs.excel.api;

import java.io.File;

import com.jytec.cs.domain.Term;

public class ImportParams {
	/**
	 * use to dispatch importer <p>
	 * 
	 * class-course, sites, schedule, training-schedule
	 * 
	 */
	public String template;
	public File file;
	public Term term;
	/** for:[schedule/training-schedule] */
	public int classYear;
	public boolean ignorePreview;

	public void validate() {

	}

	public ImportParams file(File file) {
		this.file = file;
		return this;
	}

	public ImportParams term(Term term) {
		this.term = term;
		return this;
	}
	
	public ImportParams classYear(int classYear) {
		this.classYear = classYear;
		return this;
	}

	public static ImportParams create() {
		return new ImportParams();
	}

	public ImportParams ignorePreview() {
		this.ignorePreview = true;
		return this;
	}

}
