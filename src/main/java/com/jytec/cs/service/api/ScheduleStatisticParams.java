package com.jytec.cs.service.api;

import java.util.HashMap;
import java.util.Map;

public class ScheduleStatisticParams extends ScheduleSearchParams {
	private static final Map<String, String> allowedGroupByProps;
	static {
		String props = "termYear,termMonth,weekno,dayOfWeek,date,timeStart,timeEnd,trainingType";
		allowedGroupByProps = new HashMap<>();
		for (String prop : props.split(",")) {
			allowedGroupByProps.put(prop, prop);
		}
		allowedGroupByProps.put("classId", "theClass.id");
		allowedGroupByProps.put("courseCode", "course.code");
		allowedGroupByProps.put("siteId", "site.id");
		allowedGroupByProps.put("teacherId", "teacher.id");
		allowedGroupByProps.put("majorId", "theClass.major.id");
		allowedGroupByProps.put("deptId", "theClass.deptId");
		allowedGroupByProps.put("classYear", "theClass.year");
		allowedGroupByProps.put("courseCate", "course.cate");

	}

	private String aggFields; // aggregate group name
	private String groupBy;
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
				String realProp = allowedGroupByProps.get(prop.trim());
				if (realProp != null) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(rootAlias);
					sb.append(".");
					sb.append(realProp);
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
}
