package com.jytec.cs.excel;

import java.io.IOException;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.jytec.cs.excel.api.ImportParams;

public abstract class AbstractImporter implements ApplicationContextAware {
	protected static Log log = LogFactory.getLog(AbstractImporter.class.getPackage().getName());
	protected ApplicationContext applicationContext;

	@Transactional
	public void importFile(ImportParams params) throws EncryptedDocumentException, IOException {
		Assert.notNull(params.file, "未指定导入文件。");
		Assert.isTrue(params.file.exists(), "文件不存在！" + params.file.getAbsolutePath());

		log.debug("准备处理文件：" + params.file.getAbsolutePath());
		try (Workbook wb = WorkbookFactory.create(params.file, null, true)) {
			ImportContext context = new ImportContext();
			context.params = params;
			context.modelHelper = getModelHelper(params);

			doImport(wb, context);
		}
		log.debug("文件处理结束：" + params.file.getAbsolutePath());

	}

	/**
	 * Default implementation will only for-each sheet call doImport(sheet, context). <p>
	 * 
	 * Subclass may be override the method, do some initialization and call super.
	 */
	protected void doImport(Workbook wb, ImportContext context) {
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			doImport(sheet, context);
		}
	}

	protected abstract void doImport(Sheet sheet, ImportContext context);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected ModelMappingHelper getModelHelper(ImportParams params) {
		// return applicationContext.getBean(ModelMappingHelper.class, params.term);
		return applicationContext.getBean(ModelMappingHelper.class).term(params.term);
	}
}