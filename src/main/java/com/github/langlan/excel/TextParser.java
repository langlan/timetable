package com.github.langlan.excel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;

import com.github.langlan.domain.Class;
import com.github.langlan.domain.Major;

public interface TextParser {
	// MajorYY-No[Degree] //
	final Pattern CLASS_NAME_WITH_DEGREE = Pattern.compile("(.+?)(\\d+)-(\\d+)\\[+(.+)\\]");
	final Pattern MAJOR = Pattern.compile("(.+?)\\[+(.+)\\]");
	final Pattern MALFORMED_DEGREE = Pattern.compile("\\[{2,}|\\]{2,}");
	// NOTE: we use '[+' instead of '[' for stripping out the malformed text.

	public static String handleMalFormedDegree(String nameWithDegree) {
		// NOTE: We do not use 【text.replaceAll("(\\[|\\]){2,}", "$1")】,
		// cause it not support empty name, although no such data example appeared.
		// Also, I do not want two step procession witch first replace '\[{2,}' then '\]{2,}'
		// although it more simple in coding.
		Matcher m = MALFORMED_DEGREE.matcher(nameWithDegree);
		boolean result = m.find();
		if (result) {
			StringBuffer sb = new StringBuffer();
			do {
				m.appendReplacement(sb, m.group().substring(0, 1));
				result = m.find();
			} while (result);
			m.appendTail(sb);
			return sb.toString();
		}
		return nameWithDegree;
	}

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

	Pattern INTTEGER = Pattern.compile("\\d+");

	/** find the first digital sequence and parse */
	public static int parseInt(String text) {
		Matcher m = INTTEGER.matcher(text);
		if (m.find()) {
			Integer.parseInt(m.group());
		}
		return 0;
	}

	public static String firstIntStr(String text) {
		Matcher m = INTTEGER.matcher(text);
		if (m.find()) {
			return m.group();
		}
		return null;
	}

	/** always return a not null string, trimed */
	public static String cellString(Cell cell) {
		String text = cell != null ? cell.toString() : "";
		return text.trim();
	}

	static final Pattern timeRangePattern = Pattern.compile("(\\d+)-(\\d+)\\((\\d+),(\\d+)\\)");

	public static byte[] parseTimeRange(String timeRange) {
		Matcher m = timeRangePattern.matcher(timeRange);
		if (m.find()) {
			byte[] ret = new byte[4];
			for (int i = 0; i < 4; i++)
				ret[i] = Byte.parseByte(m.group(i + 1));
			return ret;
		}
		return null;
	}

}
