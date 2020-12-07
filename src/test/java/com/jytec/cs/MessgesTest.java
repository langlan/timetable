package com.jytec.cs;
import org.junit.jupiter.api.Test;

import com.jytec.cs.excel.parse.Messages;

public class MessgesTest {
	@Test
	public void testMessages() {
		System.out.println(Messages.format("header-row-not-found", "我们", "你"));
		System.out.println(Messages.format("header-row-not-found", "21", "你"));
	}
}
