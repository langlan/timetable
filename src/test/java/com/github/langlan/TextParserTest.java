package com.github.langlan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.langlan.excel.TextParser;

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

}
