package com.jytec.cs.excel.parse;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public interface Messages {

	static String format(String key, Object... args) {
		String text = ResourceBundle.getBundle(Messages.class.getName()).getString(key);
		return MessageFormat.format(text, args);
	}

}
