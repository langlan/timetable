package com.jytec.cs.excel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportReport;

public class ImportContext {
	public ImportParams params;
	public ImportReport report = new ImportReport();
	public ModelMappingHelper modelHelper;
	private Map<String, Object> attributes = new HashMap<>();

	public <T> T getAttribute(String key, Supplier<T> supplier) {
		@SuppressWarnings("unchecked")
		T value = (T) attributes.get(key);
		if (value == null) {
			value = supplier.get();
			attributes.put(key, value);
		}
		return value;
	}
}
