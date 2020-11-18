package com.github.langlan.excel;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.langlan.dao.DeptRepository;
import com.github.langlan.dao.TeacherRepository;

@Service
public class RoomImporer {
	private @Autowired DeptRepository deptRepository;
	private @Autowired TeacherRepository teacherRepository;

	public void doImport(File file) throws EncryptedDocumentException, IOException {
		Workbook wb = WorkbookFactory.create(file, null, true);
		doImport(wb);
		wb.close();
	}

	protected void doImport(Workbook wb) {
		// assertEquals(1, wb.getNumberOfSheets());

		Sheet sheet = wb.getSheetAt(0);

		
	}

}
