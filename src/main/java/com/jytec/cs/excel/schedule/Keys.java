package com.jytec.cs.excel.schedule;

import java.util.Objects;

import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Schedule;

public class Keys {

	public static class CourseSiteTime {
		final Schedule schedule;

		public CourseSiteTime(Schedule schedule) {
			this.schedule = schedule;
		}

		@Override
		public int hashCode() {
			return Objects.hash(schedule.getCourse().getCode(), schedule.getSite(), schedule.getWeekno(),
					schedule.getDayOfWeek(), schedule.getTimeStart(), schedule.getTimeEnd());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CourseSiteTime) {
				CourseSiteTime o = (CourseSiteTime) obj;
				return (schedule.getCourse() == o.schedule.getCourse()
						|| schedule.getCourse().getCode().equals(o.schedule.getCourse().getCode())) //
						&& (schedule.getSite() == o.schedule.getSite()
								|| schedule.getSite().getId() == o.schedule.getSite().getId()) //
						&& schedule.getWeekno() == o.schedule.getWeekno()
						&& schedule.getDayOfWeek() == o.schedule.getDayOfWeek()
						&& schedule.getTimeStart() == o.schedule.getTimeStart()
						&& schedule.getTimeEnd() == o.schedule.getTimeEnd();
			}
			return false;
		}
	}
	
	public class ClassCourseDay {
		final ClassCourse classCourse;

		public ClassCourseDay(ClassCourse schedule) {
			this.classCourse = schedule;
		}

//		@Override
//		public int hashCode() {
//			return Objects.hash(classCourse.getTheClass().getId(), classCourse.getCourse().getCode(), classCourse.getWeekno(),
//					classCourse.getDayOfWeek());
//		}
	//
//		@Override
//		public boolean equals(Object obj) {
//			if (obj == null || !(obj instanceof ClassCourseDay)) {
//				return false;
//			}
//			ClassCourseDay c = (ClassCourseDay) obj;
//			return classCourse.getTheClass().getId() == c.classCourse.getTheClass().getId()
//					&& classCourse.getCourse().getCode().equals(c.classCourse.getCourse().getCode())
//					&& classCourse.getWeekno() == c.classCourse.getWeekno()
//					&& classCourse.getDayOfWeek() == c.classCourse.getDayOfWeek();
//		}
	}
}
