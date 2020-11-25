package com.jytec.cs.domain;

import java.util.HashMap;
import java.util.Map;

/** 0 represents default or unknown */
public class Dict {
	final String[] values;
	final Map<String, Byte> codes;
	public static final Dict courseCat = new Dict("无", "公共基础课,专业技能课,专业技能课（选）,实习环节");

	private Dict(String defaultText, String text) {
		String[] meaningful = text.split(",");
		this.values = new String[meaningful.length + 1];
		System.arraycopy(meaningful, 0, this.values, 1, meaningful.length);
		this.values[0] = defaultText;

		codes = new HashMap<String, Byte>();
		for (byte i = 1; i < values.length; i++) {
			codes.put(values[i], (byte) i);
		}
		if (defaultText != null) {
			codes.put(defaultText, (byte) 0);
		}
	}

	/** default 0 */
	public byte getCode(String text) {
		return codes.getOrDefault(text, (byte) 0);
	}

	public byte getCodeOrDefault(String text, byte defaultValue) {
		return codes.getOrDefault(text, defaultValue);
	}

	public byte getCodeOrThrows(String text) {
		Byte ret = codes.get(text);
		if (ret == null) {
			throw new IllegalArgumentException("未知类型：" + text);
		}
		return ret;
	}

	public String get(byte code) {
		return values[code];
	}
}