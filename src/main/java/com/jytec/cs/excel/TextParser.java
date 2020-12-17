package com.jytec.cs.excel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TitleInfo.TimeInfo;
import com.jytec.cs.excel.exceptions.IllegalFormatException;

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
	 * only set name.
	 * 
	 * @param deptName
	 * @return if name not empty. or null.
	 */
	public static Dept parseDept(String deptName) {
		if (deptName != null && !deptName.trim().isEmpty()) {
			Dept dept = new Dept();
			dept.setName(deptName);
			return dept;
		}
		return null;
	}

	/**
	 * 来源：教学任务表－班级列
	 * 
	 * @param classNameWithDegree
	 * @return Class[name,year,classNo,degree, major[shortName,degree]] or null if not match.
	 */
	public static Class parseClass(String classNameWithDegree) {
		// String text = cell.toString();
		Matcher m = CLASS_NAME_WITH_DEGREE.matcher(classNameWithDegree);
		if (m.find()) {
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
		return null;
	}

	final Pattern CLASSES_NAME = Pattern.compile("(?<major>.+?)(?<year>\\d+)-" //
			+ "(?<classno>\\d+)" // classNo-start or just classNo
			+ "[\r\n]*" // optional new line
			+ "(?:[~-](?<classnoTo>\\d+))?" // classNo-end
			+ "(?:[\\[\\(（]+(?<degree>.+?)[\\]\\)）])?"); // degree: optional

	/**
	 * 来源于多个实训表，待规范。
	 * <!-- @formatter:off -->
	 * <ul> 
	 *   <li>多个班级以空格或换行分隔</li>
	 *   <li>多个连号班级以“~”或“-”分隔</li>
	 *   <li>degree 后缀 :: 中文括号“（）”或英文括号“()”或英文中括号“[]”</li>
	 *   <li>majorYear级no－no（系部）::</li> 
	 * </ul><!-- @formatter:on -->
	 * @param classesRangeText
	 * @param defaultDegree 当不带 degree 后缀时需要提供。
	 * @return
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
				if (defaultDegree == null || defaultDegree.isEmpty()) {
					throw new IllegalArgumentException("未带 degree 后缀又未提供默认值！");
				}
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
	 * 来源：教学任务表单－专业列 <p>
	 * 
	 * 注：专业简名可从班级名前缀获取。
	 * 
	 * @param majorNameWithDegree name[degree]
	 * @return Major[name, degree]. or null if pattern not match.
	 */
	public static Major parseMajor(String majorNameWithDegree /* , boolean shortName */) {
		Matcher m = MAJOR.matcher(majorNameWithDegree);
		if (m.find()) {
			String name = m.group(1);
			String degree = m.group(2);

			Major major = new Major();
			major.setDegree(degree);
			// if (shortName) {
			// major.setShortName(name);
			// } else {
			major.setName(name + "[" + degree + "]");
			// }

			return major;
		}
		return null;
	}

	public static void assertEquals(String expect, String actual) {
		if (expect == null && actual != null || !expect.equals(actual))
			throw new IllegalStateException("Expect [" + expect + "], but was [" + actual + "].");
	}

	// ========== Schedule ============
	public class TimeRange {
		public byte timeStart, timeEnd;
		// /** true: odd week only | false: even week only | null: no exclusion. */
		// public Boolean oddWeekOnly;
		public byte[] weeknos;
	}

	public class ScheduledCourse {
		public String courseName;
		public TimeRange timeRange;
		public String siteName;
		/** for theory always single, for training maybe multiple */
		public String[] teacherName;
		// for resolve
		public List<ClassCourse> classCourses;
		Site site;
		public List<Teacher> teachers;
		public byte dayOfWeek;
		public boolean resolveSuccess;
		
		private void assertReslveSuccess(){
			if (!resolveSuccess) {
				throw new IllegalStateException("存在班级选课未找到记录异常，此处不应被调用");
			}
		}

		public List<Class> getClasses() {
			assertReslveSuccess();
			return classCourses.stream().map(ClassCourse::getTheClass).collect(Collectors.toList());
		}

		public Course getCourse() {
			assertReslveSuccess();
			return classCourses.get(0).getCourse();
		}

	}

	static final Pattern WEEK = Pattern.compile("(\\d+)-?(\\d+)?");
	static final Pattern TIME_RANGE = Pattern.compile("^([\\d-,]+)" // 1: week or weeks
			+ "(单|双)?" // 2:
			+ "\\((\\d+),(\\d+)\\)"); // 3,4: time range

	/**
	 * <!--@formatter:off-->
	 * <ul>
	 *   <li>1-6(3,4): schedule-theory-1.xlsx</li>   
	 *   <li>1-2,4-6(3,4): schedule-theory-1.xlsx</li>  
	 *   <li>4-6双(1,2): schedule-theory-1.xlsx</li>  
	 *   <li>1-3单(7,8): schedule-theory-1.xlsx</li>  
	 * </ul>
	 * <!--@formatter:on-->
	 * @param timeRange
	 * @return
	 */
	public static TimeRange parseTimeRange(String timeRange) {
		Matcher m = TIME_RANGE.matcher(timeRange);
		if (m.matches()) {
			TimeRange ret = new TimeRange();
			Boolean oddWeekOnly = null;
			if (m.group(2) != null) {
				oddWeekOnly = m.group(2).equals("单");
			}
			String _week_ = m.group(1);
			String[] weekTexts = _week_.split(",");
			List<Byte> weeknos = new LinkedList<>();
			for (String weekText : weekTexts) {
				Matcher wm = WEEK.matcher(weekText);
				if (!wm.matches()) {
					throw new IllegalFormatException("周（或周区间）格式有误：" + weekText);
				}
				byte weeknoStart = Byte.parseByte(wm.group(1));
				String weeknoEndStr = wm.group(2);
				byte weeknoEnd = weeknoEndStr != null ? Byte.parseByte(weeknoEndStr) : weeknoStart;
				for (byte weekno = weeknoStart; weekno <= weeknoEnd; weekno++) {
					if (oddWeekOnly != null && ((weekno % 2 == 0) == oddWeekOnly)) {
						continue; // exclude even when odd-only, or exclude odd when even-only
					}
					weeknos.add(weekno);
				}
			}
			ret.weeknos = toPrimitives(weeknos.toArray(new Byte[weeknos.size()]));
			ret.timeStart = Byte.parseByte(m.group(3));
			ret.timeEnd = Byte.parseByte(m.group(4));
			return ret;
		}
		throw new IllegalFormatException("排课时间格式错误：" + timeRange);
	}

	static byte[] toPrimitives(Byte[] oBytes) {
		byte[] bytes = new byte[oBytes.length];
		for (int i = 0; i < oBytes.length; i++) {
			bytes[i] = oBytes[i];
		}
		return bytes;

	}

	/**
	 * 单个课程格式：courseName<>timeRange<>siteName<>teacherName <p>
	 * 
	 * 多个课程之间以换行分隔
	 * 
	 * @param text 单元格内容（一个或多个课程）
	 * @return an array of schedule-info
	 */
	public static ScheduledCourse[] parseSchedule(String text) throws IllegalFormatException {
		String[] lines = breakLines(text);
		ScheduledCourse[] ret = new ScheduledCourse[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] values = line.split("<>");
			if (values.length != 4)
				throw new IllegalFormatException("理论课数据格式异常（应通过<>分隔为四段）:" + line);
			ScheduledCourse schedule = new ScheduledCourse();
			schedule.courseName = values[0];
			schedule.timeRange = parseTimeRange(values[1]);
			schedule.siteName = values[2];
			String teacherName = values[3];
			schedule.teacherName = (teacherName == null || teacherName.isEmpty() ? new String[0]
					: new String[] { teacherName });
			ret[i] = schedule;
		}
		return ret;
	}

	/**
	 * 实训排课
	 * 
	 * @param text               单元格内容（一个课程）
	 * @param weeknos            学周（可能多个，来自行头）
	 * @param timeRange（学时，来自列头）
	 * @return
	 */
	public static ScheduledCourse[] parseTrainingSchedule(String text, byte[] weeknos, TimeInfo timeRange) {
		String[] lines = breakLines(text);
		ScheduledCourse[] ret = new ScheduledCourse[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] values = line.split(" ");
			if (values.length != 3)
				throw new IllegalArgumentException("import.excel.training-schedule :" + line);
			ScheduledCourse schedule = new ScheduledCourse();
			schedule.courseName = values[0].trim();
			schedule.siteName = values[1].trim();
			String teacherName = values[2].trim();
			if (teacherName == null || teacherName.isEmpty()) {
				schedule.teacherName = new String[0];
			} else if (teacherName.contains(",")) {
				schedule.teacherName = teacherName.split(",");
			} else if (teacherName.contains("、")) {
				schedule.teacherName = teacherName.split("、");
			} else {
				schedule.teacherName = new String[] { teacherName };
			}

			schedule.timeRange = new TimeRange();
			if (weeknos != null) {
				schedule.timeRange.weeknos = weeknos;
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

	public static byte[] parseTrainingWeeknoRange(String text) {
		Matcher m = WEEK_RANGE.matcher(text);
		if (m.find()) {
			byte weeknoStart = Byte.parseByte(m.group(1));
			String weeknoEndStr = m.group(2);
			byte weeknoEnd = (weeknoEndStr != null ? Byte.parseByte(weeknoEndStr) : weeknoStart);
			Byte[] ret = new Byte[weeknoEnd - weeknoStart + 1];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = (byte) (weeknoStart + i);
			}
			return toPrimitives(ret);
		}
		return null;
	}

	public static String[] breakLines(String text) {
		String lineBreakPattern = "(\\s*)?[\r\n]+(\\s*)?";
		String[] lines = text.split(lineBreakPattern);
		return lines;
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
