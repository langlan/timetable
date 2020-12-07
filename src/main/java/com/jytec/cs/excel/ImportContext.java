package com.jytec.cs.excel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportPreview;


public class ImportContext {
	public ImportParams params;
	public ImportPreview preview = new ImportPreview();
	public ModelMappingHelper modelHelper;
	
	int countSheetIgnored = 0;

	Set<String> classCourseKeys = new HashSet<>();
	Map<String, Dept> depts = new HashMap<>();
	Map<String, Major> majors = new HashMap<>();
	Map<String, Class> classes = new HashMap<>();
	

	public void clearCaches() {
		Collection<?> empty = Collections.emptyList();
		Map<?, ?> emptyMap = Collections.emptyMap();
		(classCourseKeys != null ? classCourseKeys : empty).clear();
		(depts != null ? depts : emptyMap).clear();
		(majors != null ? majors : emptyMap).clear();
		(classes != null ? classes : emptyMap).clear();
		classCourseKeys = null;
		depts = null;
		majors = null;
		classes = null;
	}


	public void ignoreSheet(String message) {
		countSheetIgnored ++;
	}
}
