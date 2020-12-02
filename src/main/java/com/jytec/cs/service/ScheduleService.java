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

@Service
public class ScheduleService extends ModelService<Schedule> {

	@Transactional
	public List<Schedule> search(ScheduleSearchParams params) {
		Sql ql = new Sql().select("m").from("Schedule m").where() //@formatter:off
			.apply(it -> applySearchParams(it, params))
		.endWhere()
		.orderBy("date,timeStart"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	void applySearchParams(WhereFragment<Sql> where, ScheduleSearchParams params) {
		where //@formatter:off
			.eq("m.termYear", params.termYear)
			.eq("m.termMonth", params.termMonth)
			.eq("m.weekno", params.weekno)
			.eq("m.dayOfWeek", params.dayOfWeek)
			.eq("m.timeStart", params.timeStart)
			.eq("m.timeEnd", params.timeEnd)
			.eq("m.trainingType", params.trainingType)
			.eq("m.date", params.date)
			.like("m.date", params.yearMonth, false, true)
			// model-id
			.eq("m.theClass.id", params.classId)
			.eq("m.course.code", params.courseCode)
			.eq("m.teacher.id", params.teacherId)
			.eq("m.site.id", params.siteId)
		;//@formatter:on
	}

	@Transactional
	public List<Map<String, Object>> statistic(ScheduleStatisticParams statParams) {
		String groupByClause = statParams.prepareAggFields().groupBy("m");
		String groupByAsSelectItems = statParams.groupByAsSelectItems("m");
		Sql ql = new Sql() //@formatter:off
		.select()
			.____(groupByAsSelectItems)
			.____("count(*) as recordCount")                            .$(statParams.aggRecordCount)
			.____("sum(m.timeEnd-m.timeStart+1) as lessonTime")         .$(statParams.aggLessonTime)
			.____("sum(m.timeEnd-m.timeStart+1)/2 as lessonCount")      .$(statParams.aggLessonCount)
		.from("Schedule m") 
		.where() 
			.apply(it -> applySearchParams(it, statParams))
		.endWhere()
		.groupBy(groupByClause); //@formatter:on

		return dao.findMaps(ql.toString(), ql.vars());
	}
}