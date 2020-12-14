package com.jytec.cs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;

public class TextParserTest {
	@Test
	public void testMalformedDegree() {
		String normal = "[name]";
		assertTrue(normal == TextParser.handleMalFormedDegree(normal));
		assertEquals(normal, TextParser.handleMalFormedDegree("[[name]"));
		// following format are not found in data, but just make it robust.
		assertEquals(normal, TextParser.handleMalFormedDegree("[name]]"));
		assertEquals(normal, TextParser.handleMalFormedDegree("[[name]]"));
		assertEquals("[]", TextParser.handleMalFormedDegree("[]"));
		assertEquals("[]", TextParser.handleMalFormedDegree("[[]"));
		assertEquals("[]", TextParser.handleMalFormedDegree("[]]"));
		assertEquals("[]", TextParser.handleMalFormedDegree("[[]]"));
	}

	@Test
	public void testLineSplitWithTrim() {
		System.out.println("\\r\\n".trim().isEmpty());
		System.out.println(Arrays.asList("abc".split("[\\r\\n]+")));
		System.out.println(Arrays.asList("abc \r\n  def".split("\\s*[\r\n]+\\s*")));
		System.out.println(Arrays.asList("abc ,  def".split("\\s*,\\s*")));
	}

	@Test
	public void testSplitTeacher() {
		assertArrayEquals(new String[] { "王大小" }, "王大小".split("/"));
		assertArrayEquals(new String[] { "王大小", "李中二" }, "王大小/李中二".split("/"));
	}

	@Test
	public void testParseTimeRange() {
		TimeRange timeRange = TextParser.parseTimeRange("1-6(3,4)");
		assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6 }, timeRange.weeknos);
		assertEquals(3, timeRange.timeStart);
		assertEquals(4, timeRange.timeEnd);

		timeRange = TextParser.parseTimeRange("1-2,4-6(3,4)");
		assertArrayEquals(new byte[] { 1, 2, 4, 5, 6 }, timeRange.weeknos);
		assertEquals(3, timeRange.timeStart);
		assertEquals(4, timeRange.timeEnd);

		timeRange = TextParser.parseTimeRange("4-6双(1,2)");
		assertArrayEquals(new byte[] { 4, 6 }, timeRange.weeknos);
		assertEquals(1, timeRange.timeStart);
		assertEquals(2, timeRange.timeEnd);

		timeRange = TextParser.parseTimeRange("1-3单(7,8)");
		assertArrayEquals(new byte[] { 1, 3 }, timeRange.weeknos);
		assertEquals(7, timeRange.timeStart);
		assertEquals(8, timeRange.timeEnd);
	}

	@Test
	public void testParseClasses() { // 物流19（三二）
		// standard+
		Class[] classes = TextParser.parseClasses("理化20-1[高职]理化20-2[高职]", null);
		assertEquals("理化20-1[高职]", classes[0].getName());
		assertEquals("理化20-2[高职]", classes[1].getName());
		assertEquals("高职", classes[0].getDegree());
		assertEquals("高职", classes[1].getDegree());
		assertEquals(2, classes.length);
		// single without degree
		classes = TextParser.parseClasses("理化20-1", "高职");
		assertEquals("理化20-1[高职]", classes[0].getName());
		assertEquals("高职", classes[0].getDegree());
		assertEquals(1, classes.length);
		// range(~, with degree)
		classes = TextParser.parseClasses("会计19-1~2（三二）", null);
		assertEquals("会计19-1[三二]", classes[0].getName());
		assertEquals("会计19-2[三二]", classes[1].getName());
		assertEquals("三二", classes[0].getDegree());
		assertEquals("三二", classes[1].getDegree());
		assertEquals(2, classes.length);
		// range(~, without degree)
		classes = TextParser.parseClasses("物流18-1~2", "高职");
		assertEquals("物流18-1[高职]", classes[0].getName());
		assertEquals("物流18-2[高职]", classes[1].getName());
		assertEquals("高职", classes[0].getDegree());
		assertEquals("高职", classes[1].getDegree());
		assertEquals(2, classes.length);

	}

	@Test
	public void testPraseTrainingSchedule() {
		ScheduledCourse[] schedules = TextParser.parseTrainingSchedule("牛牛检测实训 JJ-109 吕牛牛", null, null);
		assertEquals(1, schedules.length);
		assertEquals("牛牛检测实训", schedules[0].course);
		assertEquals("JJ-109", schedules[0].site);
		assertEquals("吕牛牛", schedules[0].teacher);
	}

	@Test
	public void testParseTerm() {
		Term term = TextParser.parseTerm("2020-2021学年第一学期");
		assertNotNull(term);
		assertEquals(2020, term.getTermYear());
		assertEquals(Term.TERM_MONTH_AUTUMN, term.getTermMonth());
		term = TextParser.parseTerm("2020-2021学年第二学期");
		assertEquals(2021, term.getTermYear());
		assertEquals(Term.TERM_MONTH_SPRING, term.getTermMonth());
		term = TextParser.parseTerm("2021学年第二学期");
		assertEquals(2021, term.getTermYear());
		assertEquals(Term.TERM_MONTH_SPRING, term.getTermMonth());

		term = TextParser.parseTerm("2020学年秋季学期");
		assertEquals(2020, term.getTermYear());
		assertEquals(Term.TERM_MONTH_AUTUMN, term.getTermMonth());
		term = TextParser.parseTerm("2021学年春季学期");
		assertEquals(2021, term.getTermYear());
		assertEquals(Term.TERM_MONTH_SPRING, term.getTermMonth());
	}

}
