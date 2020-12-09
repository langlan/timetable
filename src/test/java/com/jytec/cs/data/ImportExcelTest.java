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
import com.jytec.cs.excel.TrainingScheduleImporter;
import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportReport;

@SpringBootTest
public class ImportExcelTest {
	private @Autowired ClassCourseImporter classCourseImporter;
	private @Autowired SiteImporter roomImporter;
	private @Autowired ScheduleImporter scheduleImporter;
	private @Autowired TrainingScheduleImporter trainingScheduleImporter;
	// private @Autowired DeptRepository deptRepository;
	// private @Autowired MajorRepository majorRepository;

	@Test
	public void testImport00ClassCourses() throws EncryptedDocumentException, IOException {
		File file = new File("C:/Users/langlan/Desktop/课表/basic-class-course.xls");
		ImportParams params = ImportParams.create().file(file).term(TermSerivcelTest.TERM);
		ImportReport report = classCourseImporter.importFile(params.preview());
		System.out.println(report);
		// classCourseImporter.importFile(params);
	}

	@Test
	public void testImport00Rooms() throws EncryptedDocumentException, IOException {
		File file = new File("C:/Users/langlan/Desktop/课表/basic-rooms.xlsx");
		roomImporter.importFile(file);
	}

	@Test
	public void testImport01ScheduleOfTheoryCourse() throws EncryptedDocumentException, IOException {
		File file = new File("C:/Users/langlan/Desktop/课表/schedule-theory-1.xlsx");
		ImportParams params = ImportParams.create().term(TermSerivcelTest.TERM).classYear(19);
		scheduleImporter.importFile(params.file(file));
		file = new File("C:/Users/langlan/Desktop/课表/schedule-theory-2.xlsx");
		scheduleImporter.importFile(params.file(file));
	}

	@Test
	public void testImport02ScheduleOfTrainingCourse() throws EncryptedDocumentException, IOException {
		ImportParams params = ImportParams.create().term(TermSerivcelTest.TERM).classYear(19);
		File file = new File("C:/Users/langlan/Desktop/课表/schedule-training-1.xlsx");
		trainingScheduleImporter.importFile(params.file(file));
		// file = new File("C:/Users/langlan/Desktop/课表/schedule-theory-2.xlsx");
		// scheduleImporter.importFile(TermSerivcelTest.TERM, 19, file);
	}

}