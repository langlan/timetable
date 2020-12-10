package com.jytec.cs.excel.api;

import java.io.File;

import com.jytec.cs.domain.Term;

public class ImportParams {
	public File file;
	public Term term;
	/** not commit saving */
	public boolean preview;
	/** for:[schedule/training-schedule] */
	public int classYear;
	/** for:[schedule/training-schedule] */
	public boolean suppressClassCourseNotFoundError;
	public boolean suppressTeacherNotFoundException;
	public boolean suppressTeacherNotMatchException;

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
	
	public ImportParams suppressClassCourseNotFoundError() {
		this.suppressClassCourseNotFoundError = true;
		return this;
	}

	public static ImportParams create() {
		return new ImportParams();
	}

	public ImportParams preview() {
		this.preview = true;
		return this;
	}

}
