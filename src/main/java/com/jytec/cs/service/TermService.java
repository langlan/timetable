package com.jytec.cs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.DateRepository;
import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.dao.TermRepository;
import com.jytec.cs.dao.WeekRepository;
import com.jytec.cs.domain.Term;
import com.jytec.cs.domain.Week;
import com.jytec.cs.util.Dates;
import com.jytec.cs.util.Dates.CalenderWrapper;

@Service
public class TermService {
	private static final Log log = LogFactory.getLog(TermService.class);

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
	public void initTermDate(short termYear, byte termMonth, Date firstWeek, int numberOfWeeks) {
		Assert.notNull(firstWeek, "请选择一个日期将其所在周做为第一个学周！");

		Term term = Term.of(termYear, termMonth); // also validated.
		termRepository.findById(term.getId()).orElseGet(() -> termRepository.save(term));

		CalenderWrapper cw = Dates.wrapper().letMondayFirst()
				.with(new SimpleDateFormat(com.jytec.cs.domain.Date.DATE_FORMAT));
		cw.go(firstWeek);

		// TODO: delete existing weeks and dates
		// or rest
		// week[termYear, termMonth, weekno]
		// date[weekno]
		// firstWeek = weekRepository.findById(fomatter.format(theMondyOfFirstWeek)).orElse(other);
		for (int i = 0; i < numberOfWeeks; i++) {
			Week week = new Week();
			week.setFirstDay(cw.goMonday().format());
			week.setLastDay(cw.goSunday().format());
			week.setWeekno((byte) (i + 1)); // 1 based.
			week.setTermYear(termYear);
			week.setTermMonth(termMonth);
			weekRepository.save(week);

			// initialize date
			com.jytec.cs.domain.Date day = new com.jytec.cs.domain.Date();
			cw.goMonday();
			for (int ii = 0; ii < 7; ii++) {
				day.setDate(cw.format());
				day.setDayOfWeek((byte) (ii + 1)); // 1 based.
				day.setWeekno(week.getWeekno());
				day.setYear((short) cw.getYear());
				day.setMonth((byte) cw.getMonth());
				dateRepository.save(day);
				cw.addDay(1); // go forward one day.
			}

			// cw.addWeek(1); already go forward in day-loop
		}
		log.info("Count for rebuilding schedule date: " + rebuildScheduleDate(term));
	}

	@Transactional
	public int rebuildScheduleDate(Term term) {
		return scheduleRepository.updateDateByTerm(term.getTermYear(), term.getTermMonth());
	}

	@Transactional
	public Term getOrCreateTerm(short termYear, byte termMonth) {
		Term _term = Term.of(termYear, termMonth);
		Optional<Term> term = termRepository.findById(_term.getId());
		return term.orElseGet(() -> termRepository.save(_term));
	}

}
