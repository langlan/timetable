package com.github.langlan.excel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.langlan.domain.Class;
import com.github.langlan.domain.Major;

public interface TextParser {
	// MajorYY-No[Degree] //
	final Pattern CLASS_NAME_WITH_DEGREE = Pattern.compile("(.+?)(\\d+)-(\\d+)\\[(.+)\\]");
	final Pattern MAJOR = Pattern.compile("(.+?)\\[(.+)\\]");

	/**
	 * 
	 * @param classNameWithDegree
	 * @return a Class instance[name,year,classNo,degree] with major[shortName,degree]
	 */
	public static Class parseClass(String classNameWithDegree) {
		// String text = cell.toString();
		Matcher m = CLASS_NAME_WITH_DEGREE.matcher(classNameWithDegree);
		m.find();
		String majorShortName = m.group(1);
		String year = "20" + m.group(2);
		String classNo = m.group(3);
		String degree = m.group(4);

		Class clazz = new Class();
		Major major = new Major();
		clazz.setMajor(major);
		major.setShortName(majorShortName);
		major.setDegree(degree);
		clazz.setName(classNameWithDegree.substring(0, classNameWithDegree.indexOf('[')));
		clazz.setDegree(degree);
		clazz.setYear(Short.parseShort(year));
		clazz.setClassNo(Byte.parseByte(classNo));
		return clazz;
	}

	/**
	 * 
	 * @param text  name[degree]
	 * @param major
	 * @return the major passed in or create a Major instance with [name, degree] set.
	 */
	public static Major parseMajor(String text, Major major/* , boolean shortName */) {
		Matcher m = MAJOR.matcher(text);
		m.find();
		String name = m.group(1);
		String degree = m.group(2);

		if (major == null)
			major = new Major();
		
		major.setDegree(degree);
		// if (shortName) {
		// major.setShortName(name);
		// } else {
		major.setName(name);
		// }

		return major;
	}
	
	public static void assertEquals(String expect, String actual) {
		if (expect == null && actual != null || !expect.equals(actual))
			throw new IllegalStateException("Expect [" + expect + "], but was [" + actual + "].");
	}

}
