package com.jytec.cs.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.Assert;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TrainingScheduleImporter.TitleInfo.TimeInfo;

public interface TextParser {
	// MajorYY-No[Degree] //
	final Pattern CLASS_NAME_WITH_DEGREE = Pattern.compile("(.+?)(\\d+)-(\\d+)\\[+(.+?)\\]");
	final Pattern MAJOR = Pattern.compile("(.+?)\\[+(.+)\\]");
	final Pattern MALFORMED_DEGREE = Pattern.compile("\\[{2,}|\\]{2,}");
	// NOTE: we use '[+' instead of '[' for stripping out the malformed text.

	public static String handleMalFormedDegree(String nameWithDegree) {
		// NOTE: We do not use 【text.replaceAll("(\\[|\\]){2,}", "$1")】,
		// cause it not support empty name, although no such data example appeared.
		// Also, I do not want two step process witch at first replace '\[{2,}' then '\]{2,}'
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

	final Pattern EMPTY_LOGICAL_VALUES = Pattern.compile("[无]");

	/** return empty String if matches any predefined logical-empty-values. or return what it is originally. */
	public static String handleLogicalEmpty(String text) {
		if (EMPTY_LOGICAL_VALUES.matcher(text).matches()) {
			return "";
		}
		return text;
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
		String shortYear = m.group(2);
		String classNo = m.group(3);
		String degree = m.group(4);
		Assert.isTrue(shortYear.length() == 2, "format: class-year suppose to be 2 characters.");

		String year = "20" + shortYear;
		String classNameWithoutDegree = majorShortName + shortYear + "-" + classNo;

		Major major = new Major();
		major.setShortName(majorShortName + "[" + degree + "]");
		major.setDegree(degree);

		Class clazz = new Class();
		clazz.setMajor(major);
		clazz.setName(handleMalFormedDegree(classNameWithDegree));
		Assert.isTrue(clazz.getName().equals(classNameWithoutDegree + "[" + degree + "]"),
				"format: degree handle failed");
		clazz.setDegree(degree);
		clazz.setYear(Short.parseShort(year));
		clazz.setClassNo(Byte.parseByte(classNo));
		return clazz;
	}

	final Pattern CLASSES_NAME = Pattern.compile("(?<major>.+?)(?<year>\\d+)-" //
			+ "(?<classno>\\d+)" // classNo-start or just classNo
			+ "(?:[~-](?<classnoTo>\\d+))?" // classNo-end
			+ "(?:[\\[\\(（]+(?<degree>.+?)[\\]\\)）])?"); // degree: optional

	/**
	 * <ul>
	 * <li>standard+</li>
	 * <li>majorYear-no~no :: (with no degree or （三二）)</li>
	 * <li>majorYear级no－no（系部）::</li>
	 * </ul>
	 */
	public static Class[] parseClasses(String classesRangeText, String defaultDegree) {
		List<Class> ret = new ArrayList<>();
		Matcher m = CLASSES_NAME.matcher(classesRangeText);
		while (m.find()) {
			String majorShortName = m.group("major");
			String shortYear = m.group("year");
			int classNoStart = Integer.parseInt(m.group("classno"));
			String classNoEndStr = m.group("classnoTo");
			int classNoEnd = classNoEndStr != null ? Integer.parseInt(classNoEndStr) : classNoStart;
			String degree = m.group("degree");
			if (degree == null) {
				degree = defaultDegree;
			}
			Assert.isTrue(shortYear.length() == 2, "format: class-year suppose to be 2 characters.");

			Major major = new Major();
			major.setShortName(majorShortName + "[" + degree + "]");
			major.setDegree(degree);

			for (int classNo = classNoStart; classNo <= classNoEnd; classNo++) {
				String classNameWithoutDegree = majorShortName + shortYear + "-" + classNo;

				Class clazz = new Class();
				clazz.setMajor(major);
				clazz.setName(classNameWithoutDegree + "[" + degree + "]");
				clazz.setDegree(degree);
				clazz.setYear(Short.parseShort("20" + shortYear));
				clazz.setClassNo((byte) classNo);
				ret.add(clazz);
			}
		}
		return ret.toArray(new Class[ret.size()]);
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
		major.setName(name + "[" + degree + "]");
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

	/** Concatenate all cell strings */
	public static String rowString(Row row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			sb.append(cellString(row.getCell(i)));
		}
		return sb.toString();
	}

	// ========== Schedule ============
	static final Pattern TIME_RANGE = Pattern.compile("(\\d+)(-(\\d+))?(单|双)?\\((\\d+),(\\d+)\\)");

	public class TimeRange {
		public byte weeknoStart, weeknoEnd;
		public byte timeStart, timeEnd;
		/** true: odd week only | false: even week only | null: no exclusion. */
		public Boolean oddWeekOnly;
	}

	public class ScheduledCourse {
		public String course;
		public TimeRange timeRange;
		public String site;
		public String teacher;
	}

	public static TimeRange parseTimeRange(String timeRange) {
		Matcher m = TIME_RANGE.matcher(timeRange);
		if (m.find()) {
			TimeRange ret = new TimeRange();
			ret.weeknoStart = Byte.parseByte(m.group(1));
			ret.weeknoEnd = m.group(3) == null ? ret.weeknoStart : Byte.parseByte(m.group(3));
			if (m.group(4) != null) {
				ret.oddWeekOnly = m.group(4).equals("单");
			}

			ret.timeStart = Byte.parseByte(m.group(5));
			ret.timeEnd = Byte.parseByte(m.group(6));
			return ret;
		}
		return null;
	}

	/**
	 * When multiple, split by line break.
	 * 
	 * @param text (courseName<>timeRange<>siteName<>teacherName)+
	 * @return an array of schedule-info
	 */
	public static ScheduledCourse[] parseSchedule(String text) {
		String[] lines = breakLines(text);
		ScheduledCourse[] ret = new ScheduledCourse[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] values = line.split("<>");
			if (values.length != 4)
				throw new IllegalArgumentException("import.excel.schedule :" + line);
			ScheduledCourse schedule = new ScheduledCourse();
			schedule.course = values[0];
			schedule.timeRange = parseTimeRange(values[1]);
			schedule.site = values[2];
			schedule.teacher = values[3];
			ret[i] = schedule;
		}
		return ret;
	}

	public static ScheduledCourse[] parseTrainingSchedule(String text, TimeRange weekRange, TimeInfo timeRange) {
		String[] lines = breakLines(text);
		ScheduledCourse[] ret = new ScheduledCourse[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] values = line.split(" ");
			if (values.length != 3)
				throw new IllegalArgumentException("import.excel.training-schedule :" + line);
			ScheduledCourse schedule = new ScheduledCourse();
			schedule.course = values[0].trim();
			schedule.site = values[1].trim();
			schedule.teacher = values[2].trim();
			schedule.timeRange = new TimeRange();
			if (weekRange != null) {
				schedule.timeRange.weeknoStart = weekRange.weeknoStart;
				schedule.timeRange.weeknoEnd = weekRange.weeknoEnd;
			}
			if (timeRange != null) {
				schedule.timeRange.timeStart = timeRange.timeStart;
				schedule.timeRange.timeEnd = timeRange.timeEnd;
			}
			ret[i] = schedule;
		}
		return ret;
	}

	final Pattern WEEK_RANGE = Pattern.compile("(\\d+)(?:[^\\d]+(\\d+))?");

	public static TimeRange parseTrainingWeeknoRange(String text) {
		Matcher m = WEEK_RANGE.matcher(text);
		if (m.find()) {
			TimeRange ret = new TimeRange();
			ret.weeknoStart = Byte.parseByte(m.group(1));
			String weeknoEndStr = m.group(2);
			ret.weeknoEnd = (weeknoEndStr != null ? Byte.parseByte(weeknoEndStr) : ret.weeknoStart);
			return ret;
		}
		return null;
	}

	public static String[] breakLines(String text) {
		String lineBreakPattern = "(\\s*)?[\r\n]+(\\s*)?";
		String[] lines = text.split(lineBreakPattern);
		return lines;
	}

	public static String atLocaton(Row row) {
		return "@Sheet【" + row.getSheet().getSheetName() + "】行【" + (row.getRowNum() + 1) + "】";
	}

	public static String atLocaton(Cell cell) {
		return "@Sheet【" + cell.getSheet().getSheetName() + "】单元格【" + cell.getAddress() + "】";
	}

	static Pattern TERM_PATTERN = Pattern.compile("(\\d{4,})" // termYear : required
			+ "[^\\d]+" //
			+ "(?:\\d{4,}.*([1|一]学期)|[2|二]学期|(秋)|春)"); // season part : one of

	static Term parseTerm(String text) {
		Matcher m = TERM_PATTERN.matcher(text);
		if (m.find()) {
			int termYear = Integer.parseInt(m.group(1));
			boolean autumn = m.group(2) != null || m.group(3) != null;
			return autumn ? Term.ofAutumn(termYear) : Term.ofSpring(termYear);
		}
		return null;
	}

}
