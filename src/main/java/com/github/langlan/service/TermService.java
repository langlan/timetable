package com.github.langlan.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.github.langlan.dao.DateRepository;
import com.github.langlan.dao.TermRepository;
import com.github.langlan.dao.WeekRepository;
import com.github.langlan.domain.Term;
import com.github.langlan.domain.Week;
import com.github.langlan.util.Dates;
import com.github.langlan.util.Dates.CalenderWrapper;

@Service
public class TermService {
	private static final Log log = LogFactory.getLog(TermService.class);

	private @Autowired TermRepository termRepository;
	private @Autowired WeekRepository weekRepository;
	private @Autowired DateRepository dateRepository;

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

		CalenderWrapper cw = Dates.wrapper().letMondayFirst().with(new SimpleDateFormat("yyyyMMdd"));
		cw.go(firstWeek).getTime();
		
		// TODO: delete existing weeks and dates
		// or rest 
		//    week[termYear, termMonth, weekno]
		//    date[weekno]
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
			com.github.langlan.domain.Date day = new com.github.langlan.domain.Date();
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
	}

	@Transactional
	public Term getOrCreateTerm(short termYear, byte termMonth) {
		Term _term = Term.of(termYear, termMonth);
		Optional<Term> term = termRepository.findById(_term.getId());
		return term.orElseGet(() -> termRepository.save(_term));
	}

}
