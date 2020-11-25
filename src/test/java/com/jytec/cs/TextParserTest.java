package com.jytec.cs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import com.jytec.cs.excel.TextParser;

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
	public void testLineSpritWithTrim() {
		System.out.println("\\r\\n".trim().isEmpty());
		System.out.println(Arrays.asList("abc".split("[\\r\\n]+")));
		System.out.println(Arrays.asList("abc \r\n  def".split("(\\s*)?[\r\n]+(\\s*)?")));
	}
	
	@Test
	public void testSplitTeacher() {
		assertArrayEquals(new String[] {"王大小"}, "王大小".split("/"));
		assertArrayEquals(new String[] {"王大小", "李中二"}, "王大小/李中二".split("/"));
	}

}
