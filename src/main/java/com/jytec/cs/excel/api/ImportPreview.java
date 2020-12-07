package com.jytec.cs.excel.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;

/** output for confirm or discard */
public class ImportPreview {
	List<SheetImportPreview> sheets = new LinkedList<>();

	public void append(SheetImportPreview sheetImportPreview) {
		sheets.add(sheetImportPreview);
	}

	public static class SheetImportPreview {
		public int rowsTotal;
		public int rowsIgnore;
		private String ignoredReason;
		private Map<Integer, Set<String>> errorRows = new LinkedHashMap<>();

		LinkedHashSet<Teacher> teachers;
		LinkedHashSet<com.jytec.cs.domain.Class> classes;
		LinkedHashSet<Course> courses;
		LinkedHashSet<ClassCourse> classCourses;
		LinkedHashSet<Site> sites;
		LinkedHashSet<Site> schedules;

		public void ignoredByReason(String message) {
			ignoredReason = message;
		}

		public void invalidRow(int rowIndex, List<String> reasons) {
			Set<String> errors = errorRows.get(rowIndex);
			if (errors == null) {
				errors = new LinkedHashSet<>();
			}
			errors.addAll(reasons);
		}

		public void increseValidRow() {
			rowsTotal ++ ;
		}
	}
}
