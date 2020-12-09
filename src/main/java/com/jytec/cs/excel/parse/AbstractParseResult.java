package com.jytec.cs.excel.parse;

import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;

import langlan.sql.weaver.u.Variables;

public abstract class AbstractParseResult {
	public Row row;
	public boolean valid = true;
	public List<String> reasons = new LinkedList<>();
	protected boolean proceedIfPresent(Runnable runnable, String reason, String... values) {
		for (String value : values) {
			if (Variables.isEmpty(value)) {
				reasons.add(reason);
				this.valid = false;
			}
		}
		runnable.run();
		return true;
	}
	
}
