package com.jytec.cs.service;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.jytec.cs.domain.Schedule;
import com.jytec.cs.service.api.ScheduleSearchParams;
import com.jytec.cs.service.api.ScheduleStatisticParams;

import langlan.sql.weaver.Sql;
import langlan.sql.weaver.f.WhereFragment;
import langlan.sql.weaver.u.Variables;

@Service
public class ScheduleService extends ModelService<Schedule> {

	@Transactional
	public List<Schedule> search(ScheduleSearchParams params) {
		Sql ql = new Sql().select("m")//@formatter:off
		.from("Schedule m")
		.leftJoin("m.classes c")                    //.$(params.needJoinClasses())
		.leftJoin("m.teachers t")                   //.$(params.needjoinTeachers())
		.where() 
			.apply(it -> applySearchParams(it, params))
		.endWhere()
		.orderBy("m.date,m.timeStart,m.site.id"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	void applySearchParams(WhereFragment<Sql> where, ScheduleSearchParams params) {
		where //@formatter:off
			.eq("m.termId", params.termId)
			.eq("m.weekno", params.weekno)
			.eq("m.dayOfWeek", params.dayOfWeek)
			.eq("m.timeStart", params.timeStart)
			.eq("m.timeEnd", params.timeEnd)
			.eq("m.timeSpan", params.lessonSpan)
			.eq("m.lessonSpan", params.lessonSpan)
			.eq("m.courseType", params.courseType)
			.in("m.courseType", params.courseTypes)
			.eq("m.trainingType", params.trainingType)
			.in("m.trainingType", params.trainingTypes)
			.eq("m.date", params.date)
			.eq("m.teacherCount", params.teacherCount)
			.eq("m.classCount", params.classCount)
			// special
			.like("m.date", params.yearMonth, false, true)
			.__("? Between m.timeStart And m.timeEnd", params.lesson)    .$(Variables.isNotEmpty(params.lesson))
			// model-id
			.eq("m.course.code", params.courseCode)
			.eq("m.site.id", params.siteId)
			.eq("t.id", params.teacherId)
			.eq("c.id", params.classId)
			.eq("c.major.id", params.majorId)
			.eq("c.deptId", params.deptId)
			// join-fields
			.eq("c.year", params.classYear)
			.eq("m.course.cate", params.courseCate)
		;//@formatter:on
	}

	@Transactional
	public List<Map<String, Object>> statistic(ScheduleStatisticParams params) {
		String groupByClause = params.prepareAggFields().groupBy("m");
		String groupByAsSelectItems = params.groupByAsSelectItems("m");
		Sql ql = new Sql() //@formatter:off
		.select()
			.____(groupByAsSelectItems)
			.____("count(*) as recordCount")                            .$(params.aggRecordCount)
			.____("sum(m.timeSpan) as lessonTime")                      .$(params.aggLessonTime)
			.____("sum(m.lessonSpan) as lessonCount")                   .$(params.aggLessonCount)
		.from("Schedule m")
		.leftJoin("m.classes c")                                     .$(params.needJoinClasses())
		.leftJoin("m.teachers t")                                    .$(params.needjoinTeachers())
		.where() 
			.apply(it -> applySearchParams(it, params))
		.endWhere()
		.groupBy(groupByClause); //@formatter:on

		return dao.findMaps(ql.toString(), ql.vars());
	}
	
	@Transactional
	public Map<String, Object> statisticSummary(ScheduleStatisticParams params) {
		params.prepareAggFields();
		Sql ql = new Sql() //@formatter:off
		.select()
			.____(params.countDistinct("m"))
			.____("count(*) as recordCount")                            .$(params.aggRecordCount)
			.____("sum(m.timeSpan) as lessonTime")                      .$(params.aggLessonTime)
			.____("sum(m.lessonSpan) as lessonCount")                   .$(params.aggLessonCount)
		.from("Schedule m") 
		.leftJoin("m.classes c")                                     .$(params.needJoinClasses())
		.leftJoin("m.teachers t")                                    .$(params.needjoinTeachers())
		.where() 
			.apply(it -> applySearchParams(it, params))
		.endWhere(); //@formatter:on

		return dao.findUniqueMap(ql.toString(), ql.vars());
	}
}
