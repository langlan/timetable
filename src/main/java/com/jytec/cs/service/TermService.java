package com.jytec.cs.service;

import static com.jytec.cs.domain.Term.COUNT_OF_WEEK_MAX;
import static com.jytec.cs.domain.Term.COUNT_OF_WEEK_MIN;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.DateRepository;
import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.dao.TermRepository;
import com.jytec.cs.dao.WeekRepository;
import com.jytec.cs.dao.common.Dao;
import com.jytec.cs.domain.Term;
import com.jytec.cs.domain.Week;
import com.jytec.cs.service.api.DateSearchParams;
import com.jytec.cs.service.api.TermSearchParams;
import com.jytec.cs.service.api.WeekSearchParams;
import com.jytec.cs.util.Dates;
import com.jytec.cs.util.Dates.CalenderWrapper;

import langlan.sql.weaver.Sql;

@Service
public class TermService {
	private static final Log log = LogFactory.getLog(TermService.class);

	private @Autowired Dao dao;
	private @Autowired TermRepository termRepository;
	private @Autowired WeekRepository weekRepository;
	private @Autowired DateRepository dateRepository;
	private @Autowired ScheduleRepository scheduleRepository;

	/**
	 * Generate or complete the DATA for [Term, Week, Date].
	 * 
	 * @param firstWeek the week that this date belongs to will be treated as the first week of the term.
	 * @return
	 */
	@Transactional
	public void initTermDate(short termYear, boolean autumn, Date firstWeek, int numberOfWeeks) {
		Assert.notNull(firstWeek, "请选择一个日期将其所在周做为第一个学周！");
		Assert.isTrue(COUNT_OF_WEEK_MIN <= numberOfWeeks && numberOfWeeks <= COUNT_OF_WEEK_MAX,
				"周数【" + numberOfWeeks + "】超出可选范围【" + COUNT_OF_WEEK_MIN + "~" + COUNT_OF_WEEK_MAX + "】！");

		Term term = autumn ? Term.ofAutumn(termYear) : Term.ofSpring(termYear); // also validated.
		termRepository.findById(term.getId()).orElseGet(() -> term);

		CalenderWrapper cw = Dates.wrapper().letMondayFirst()
				.with(new SimpleDateFormat(com.jytec.cs.domain.Date.DATE_FORMAT));
		term.setFirstDay(cw.go(firstWeek).goMonday().format());
		term.setLastDay(cw.addWeek(numberOfWeeks - 1).goSunday().format());
		term.setCountOfWeeks((byte) numberOfWeeks);

		// validate overlapped
		List<Term> overlapped = termRepository.findOverlappedTerms(term.getId(), term.getFirstDay(), term.getLastDay());
		if (!overlapped.isEmpty()) {
			Iterable<?> ids = overlapped.stream()
					.map(it -> it.getName() + "[" + it.getFirstDay() + "~" + it.getLastDay() + "]")
					.collect(Collectors.toList());
			throw new IllegalArgumentException("与其它学期日历重叠（可能首周或周数设置不合适）：" + Strings.join(ids, ','));
		}

		List<Week> weeks = new LinkedList<>();
		List<com.jytec.cs.domain.Date> days = new LinkedList<>();

		cw.go(firstWeek);
		for (int i = 0; i < numberOfWeeks; i++) {
			Week week = new Week();
			week.setFirstDay(cw.goMonday().format());
			week.setLastDay(cw.goSunday().format());
			week.setWeekno((byte) (i + 1)); // 1 based.
			week.setTerm(term);
			weeks.add(week);

			// initialize date
			cw.goMonday();
			for (int ii = 0; ii < 7; ii++) {
				com.jytec.cs.domain.Date day = new com.jytec.cs.domain.Date();
				day.setTerm(term);
				day.setDate(cw.format());
				day.setDayOfWeek((byte) (ii + 1)); // 1 based.
				day.setWeekno(week.getWeekno());
				day.setYear((short) cw.getYear());
				day.setMonth((byte) cw.getMonth());
				days.add(day);
				cw.addDay(1); // go forward one day.
			}

			// cw.addWeek(1); already go forward in day-loop
		}
		weekRepository.saveAll(weeks);
		dateRepository.saveAll(days);
		termRepository.saveAndFlush(term);
		log.info("Count for rebuilding schedule date: " + rebuildScheduleDate(term));
	}

	@Transactional
	public int rebuildScheduleDate(Term term) {
		return scheduleRepository.updateDateByTerm(term.getId());
	}

	@Transactional
	public List<Term> search(TermSearchParams params) {
		Sql ql = new Sql().select("m").from("Term m").where() //@formatter:off
				.grp(true)
					.like("m.name", params.q, true, true)
				.endGrp()
				.eq("m.termYear", params.termYear)
				.eq("m.termMonth", params.termMonth)
				.eq("m.id", params.termId)
			.endWhere()
			.orderBy("m.id"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	@Transactional
	public List<Term> search(WeekSearchParams params) {
		Sql ql = new Sql().select("m").from("Week m").where() //@formatter:off
				.eq("m.termId", params.termId)
				.eq("m.weekno", params.weekno)
			.endWhere()
			.orderBy("m.termId, m.weekno"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	@Transactional
	public List<com.jytec.cs.domain.Date> search(DateSearchParams params) {
		Sql ql = new Sql().select("m").from("Date m").where() //@formatter:off
				.eq("m.termId", params.termId)
				.eq("m.weekno", params.weekno)
				.eq("m.dayOfWeek", params.dayOfWeek)
				.eq("m.holiday", params.holiday)
				.eq("m.year", params.year)
				.eq("m.month", params.month)
			.endWhere()
			.orderBy("m.date"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

}
