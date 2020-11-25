package com.jytec.cs.data;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jytec.cs.excel.ClassCourseImporter;
import com.jytec.cs.excel.ScheduleImporter;
import com.jytec.cs.excel.SiteImporter;

@SpringBootTest
public class ImportExcelTest {
	private @Autowired ClassCourseImporter classCourseImporter;
	private @Autowired SiteImporter roomImporter;
	private @Autowired ScheduleImporter scheduleImporter;
	// private @Autowired DeptRepository deptRepository;
	// private @Autowired MajorRepository majorRepository;

	@Test
	public void testImportClassCourses() throws EncryptedDocumentException, IOException {
		File file = new File("C:/Users/langlan/Desktop/课表/basic-class-course.xls");
		classCourseImporter.importFile(TermSerivcelTest.TERM, file);
	}

	@Test
	public void testImportRooms() throws EncryptedDocumentException, IOException {
		File file = new File("C:/Users/langlan/Desktop/课表/basic-rooms.xlsx");
		roomImporter.importFile(file);
	}

	@Test
	public void testImportScheduleOfTheoryCourse() throws EncryptedDocumentException, IOException {
		File file = new File("C:/Users/langlan/Desktop/课表/schedule-theory-1.xlsx");
		scheduleImporter.importFile(TermSerivcelTest.TERM, 19, file);
		file = new File("C:/Users/langlan/Desktop/课表/schedule-theory-2.xlsx");
		scheduleImporter.importFile(TermSerivcelTest.TERM, 19, file);
	}

}