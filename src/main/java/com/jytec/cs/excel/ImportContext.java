package com.jytec.cs.excel;

import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportPreview;


public class ImportContext {
	public ImportParams params;
	public ImportPreview preview = new ImportPreview();
	public ModelMappingHelper modelHelper;
}
