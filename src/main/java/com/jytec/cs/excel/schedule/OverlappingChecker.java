package com.jytec.cs.excel.schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jytec.cs.excel.schedule.Keys.ClassCourseDay;

/** For overlapping check. */
	public class OverlappingChecker {
		Map<ClassCourseDay, List<LessonTime>> flatTree = new HashMap<>();

//		public void addAll(List<Schedule> schedules, Cell cell) {
//			for (Schedule s : schedules) {
//				this.add(s, cell);
//			}
//		}

//		public void add(Schedule schedule, Cell cell) {
//			ClassCourseDay key = new ClassCourseDay(schedule);
//			LessonTime ivalue = new LessonTime(schedule.getTimeStart(), schedule.getTimeEnd(), cell);
//			List<LessonTime> lessonTimes = flatTree.get(key);
//			if (lessonTimes == null) {
//				lessonTimes = new LinkedList<>();
//				flatTree.put(key, lessonTimes);
//			} else {
//				for (LessonTime lessonTime : lessonTimes) {
//					if (lessonTime.overlappedWith(ivalue)) {
//						throw new IllegalArgumentException(
//								"课表中相同课程存在课时重叠：" + atLocaton(lessonTime.cell) + " VS " + atLocaton(ivalue.cell));
//					}
//				}
//			}
//			lessonTimes.add(ivalue);
//		}

	}