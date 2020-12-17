package com.jytec.cs.service.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.jytec.cs.excel.parse.Regex;

public class ScheduleStatisticParams extends ScheduleSearchParams {
	private static final Map<String, String> allowedGroupByProps;
	private static final Set<String> allowedCountDistinct;
	static {
		String props = "termId,weekno,dayOfWeek,date,timeStart,timeEnd,timeSpan,lessonSpan,courseType,trainingType";
		allowedGroupByProps = new HashMap<>();
		for (String prop : props.split(",")) {
			allowedGroupByProps.put(prop, prop);
		}
		allowedGroupByProps.put("courseCode", "course.code");
		allowedGroupByProps.put("siteId", "site.id");
		allowedGroupByProps.put("teacherId", "t.id");
		allowedGroupByProps.put("classId", "c.id");
		allowedGroupByProps.put("majorId", "c.major.id");
		allowedGroupByProps.put("deptId", "c.deptId");
		allowedGroupByProps.put("classYear", "c.year");
		allowedGroupByProps.put("courseCate", "course.cate");
		allowedGroupByProps.put("yearMonth", "substr({root}.date, 1, 7)");
		allowedGroupByProps.put("lesson", "sl.lesson");

		allowedCountDistinct = new HashSet<>();
		allowedCountDistinct.addAll(Arrays.asList("classId", "courseCode", "siteId", "teacherId", "majorId", "deptId"));

	}

	private String aggFields; // aggregate group name
	private String groupBy;
	private String distinct;
	/** sum(timeEnd-timeStart+1) as lessonTime */
	public boolean aggLessonTime;
	/** sum(timeEnd-timeStart+1)/2 as lessonCount */
	public boolean aggLessonCount;
	/** count(*) as recordCount */
	public boolean aggRecordCount;

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public String getAggFields() {
		return aggFields;
	}

	public void setAggFields(String aggFields) {
		this.aggFields = aggFields;
	}

	public void setDistinct(String distinct) {
		this.distinct = distinct;
	}

	public ScheduleStatisticParams prepareAggFields() {
		boolean any = false;
		if (aggFields != null) {
			for (String aggField : aggFields.split(",")) {
				boolean iany = true;
				switch (aggField.trim()) {
				case "recordCount":
					aggRecordCount();
					break;
				case "lessonTime":
					aggLessonTime();
					break;
				case "lessonCount":
					aggLessonCount();
					break;
				default:
					iany = false;
				}
				any = any || iany;
			}
		}
		if (!any) { // default: recordCount, lessonCount
			aggRecordCount().aggLessonCount();
		}
		return this;
	}

	public ScheduleStatisticParams aggRecordCount() {
		aggRecordCount = true;
		return this;
	}

	public ScheduleStatisticParams aggLessonTime() {
		aggLessonTime = true;
		return this;
	}

	public ScheduleStatisticParams aggLessonCount() {
		aggLessonCount = true;
		return this;
	}

	/** return the cleaned groupBy */
	public String groupBy(String rootAlias) {
		return groupBy(rootAlias, false);
	}

	public String groupByAsSelectItems(String rootAlias) {
		return groupBy(rootAlias, true);
	}

	private String groupBy(String rootAlias, boolean selectAlias) {
		if (groupBy != null) {
			StringBuilder sb = new StringBuilder();
			String[] props = groupBy.split(",");
			for (String prop : props) {
				prop = prop.trim();
				String realProp = allowedGroupByProps.get(prop);
				if (realProp != null) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					if (realProp.contains("{root}")) {
						realProp = realProp.replaceAll("\\{root\\}", rootAlias);
						sb.append(realProp);
					} else if (Regex.matches(CLASS_PROPS, prop) || Regex.matches(TEACHER_PROPS, prop)
							|| "lesson".equals(prop)) {
						sb.append(realProp); // hard code for join-alias
					} else {
						sb.append(rootAlias);
						sb.append(".");
						sb.append(realProp);
					}
					if (selectAlias) { // else for group-by clause.
						sb.append(" as ");
						sb.append(prop);
					}
				}
			}
			return sb.toString();
		}
		return "";
	}

	public String countDistinct(String rootAlias) {
		if (distinct != null) {
			StringBuilder sb = new StringBuilder();
			String[] props = distinct.split(",");
			for (String prop : props) {
				String realProp = allowedGroupByProps.get(prop.trim());
				if (allowedCountDistinct.contains(prop) && realProp != null) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append("Count(DISTINCT ");
					if (Regex.matches(CLASS_PROPS, prop) || Regex.matches(TEACHER_PROPS, prop)) {
						sb.append(realProp); // hard code for join-alias
					} else {
						sb.append(rootAlias);
						sb.append(".");
						sb.append(realProp);
					}
					sb.append(")");
					sb.append(" as ");
					sb.append(prop);
					sb.append("Count");
				}
			}
			return sb.toString();
		}
		return "";
	}

	static Pattern CLASS_PROPS = Pattern.compile("classId|classYear|majorId|deptId");
	static Pattern TEACHER_PROPS = Pattern.compile("classId|classYear|majorId|deptId");

	@Override
	public boolean needJoinClasses() {
		return super.needJoinClasses() || (groupBy != null && Regex.matchesPart(CLASS_PROPS, groupBy))
				|| (distinct != null && Regex.matchesPart(CLASS_PROPS, distinct));
	}

	@Override
	public boolean needjoinTeachers() {
		return super.needjoinTeachers() || (groupBy != null && Regex.matchesPart(TEACHER_PROPS, groupBy))
				|| (distinct != null && Regex.matchesPart(TEACHER_PROPS, distinct));
	}
	
	@Override
	public boolean needJoinLessons() {
		return groupBy!=null && groupBy.contains("lesson");
	}
}
