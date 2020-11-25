package com.jytec.cs.data;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jytec.cs.domain.Term;
import com.jytec.cs.service.TermService;
import com.jytec.cs.util.Dates;

@SpringBootTest
public class TermSerivcelTest {
	private @Autowired TermService termService;
	public static final short TERM_YEAR = 2020;
	public static final byte TERM_MONTH = 9;
	public static final Date FIRST_WEEK = Dates.of(TERM_YEAR, TERM_MONTH, 7);
	public static final int NUMBER_OF_WEEKS = 17;
	public static final Term TERM = Term.of(TERM_YEAR, TERM_MONTH);

	@Test
	public void testInitWeekAndDate() {
		termService.initTermDate(TERM_YEAR, TERM_MONTH, FIRST_WEEK, NUMBER_OF_WEEKS);
	}

}
