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
	/** for:[schedule/training-schedule], ignore such rows and save others. */
	public boolean saveOnClassCourseNotFound;
	/** force save even not match */
	public boolean saveOnTeacherNotMatch;
	/** force save even not find, auto-create */
	public boolean saveOnTeacherNotFound;
	/** force save even not find, auto-create */
	public boolean saveOnSiteNotFound;
	
	public void setTerm(String termId) {
		this.term = Term.of(termId);
	}
	
	public void setClassYear(int classYear) {
		this.classYear = classYear;
	}
	
	public void setPreview(boolean preview) {
		this.preview = preview;
	}
	
	public void setSaveOnClassCourseNotFound(boolean saveOnClassCourseNotFound) {
		this.saveOnClassCourseNotFound = saveOnClassCourseNotFound;
	}
	
	public void setSaveOnSiteNotFound(boolean saveOnSiteNotFound) {
		this.saveOnSiteNotFound = saveOnSiteNotFound;
	}
	
	public void setSaveOnTeacherNotFound(boolean saveOnTeacherNotFound) {
		this.saveOnTeacherNotFound = saveOnTeacherNotFound;
	}
	
	public void setSaveOnTeacherNotMatch(boolean saveOnTeacherNotMatch) {
		this.saveOnTeacherNotMatch = saveOnTeacherNotMatch;
	}

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

	public ImportParams saveOnClassCourseNotFound() {
		this.saveOnClassCourseNotFound = true;
		return this;
	}

	public ImportParams saveOnTeacherNotMatch() {
		this.saveOnTeacherNotMatch = true;
		return this;
	}

	public ImportParams saveOnTeacherNotFound() {
		this.saveOnTeacherNotFound = true;
		return this;
	}

	public ImportParams saveOnSiteNotFound() {
		this.saveOnSiteNotFound = true;
		return this;
	}

	public ImportParams saveOnAllErrorTypes() {
		return saveOnClassCourseNotFound().saveOnTeacherNotMatch().saveOnTeacherNotFound().saveOnSiteNotFound();
	}

	public static ImportParams create() {
		return new ImportParams();
	}

	public ImportParams preview() {
		this.preview = true;
		return this;
	}

}
