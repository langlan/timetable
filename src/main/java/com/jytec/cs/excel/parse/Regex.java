package com.jytec.cs.excel.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Regex {

	static boolean matchesPart(Pattern pattern, String input) {
		return pattern.matcher(input).find();
	}
	
	static boolean matches(Pattern pattern, String input) {
		return pattern.matcher(input).matches();
	}

	static String group(int group, Pattern pattern, String input) {
		Matcher m = pattern.matcher(input);
		if (m.find()) {
			return m.group(group);
		}
		return null;
	}

	static String[] groups(Pattern pattern, String input, int... groups) {
		Matcher m = pattern.matcher(input);
		if (m.find()) {
			int len = groups.length != 0 ? groups.length : m.groupCount() + 1;
			String[] ret = new String[len];
			for (int i = 0; i < len; i++) {
				ret[i] = m.group(groups.length != 0 ? groups[i] : i);
			}
			return ret;
		}
		return null;
	}

}
