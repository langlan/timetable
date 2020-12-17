package com.jytec.cs.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Site;
import com.jytec.cs.excel.ClassCourseImporter;
import com.jytec.cs.excel.ScheduleImporter;
import com.jytec.cs.excel.SiteImporter;
import com.jytec.cs.excel.TrainingScheduleImporter;
import com.jytec.cs.excel.TrainingScheduleImporter2;
import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportReport;
import com.jytec.cs.service.AuthService;

@SpringBootTest
public class ImportExcelTest {
	private @Autowired ClassCourseImporter classCourseImporter;
	private @Autowired SiteImporter roomImporter;
	private @Autowired ScheduleImporter scheduleImporter;
	private @Autowired TrainingScheduleImporter trainingScheduleImporter;
	private @Autowired TrainingScheduleImporter2 trainingScheduleImporter2;
	private @Autowired ScheduleRepository scheduleRepository;
	private @Autowired SiteRepository siteRepository;
	private @Autowired AuthService authService;
	int classYearFilter = 19;

	@Test
	public void testImport00ClassCourses() throws EncryptedDocumentException, IOException {
		authService.restoreIdcs(AuthServiceTest.lastBackupFile);
		File file = Files.of("basic-class-course.xls");
		ImportParams params = ImportParams.create().file(file).term(TermSerivcelTest.TERM);
		ImportReport report = classCourseImporter.importFile(params.preview());
		System.out.println(report);
		// classCourseImporter.importFile(params);
	}

	@Test
	public void testImport00Rooms() throws EncryptedDocumentException, IOException {
		File file = Files.of("/basic-rooms.xlsx");
		ImportReport rpt = roomImporter.importFile(ImportParams.create().file(file));
		System.out.println(rpt);
	}

	@Test
	public void testImport01ScheduleOfTheoryCourse() throws EncryptedDocumentException, IOException {
		File file1 = Files.of("/schedule-theory-1.xlsx");
		File file2 = Files.of("/schedule-theory-2.xlsx");
		ImportParams params = ImportParams.create().term(TermSerivcelTest.TERM).classYear(classYearFilter);
		params.saveOnAllErrorTypes();

		ImportReport rpt1 = scheduleImporter.importFile(params.file(file1));
		ImportReport rpt2 = scheduleImporter.importFile(params.file(file2));
		System.out.println(rpt1);
		System.out.println(rpt2);
	}

	@Test
	public void testImport021ScheduleOfTrainingCourse() throws EncryptedDocumentException, IOException {
		File file = Files.of("/schedule-training-1.xlsx");
		ImportParams params = ImportParams.create().term(TermSerivcelTest.TERM).classYear(classYearFilter);
		params.saveOnAllErrorTypes();

		ImportReport rpt = trainingScheduleImporter.importFile(params.file(file));
		System.out.println(rpt);
	}

	@Test
	public void testImport022ScheduleOfTrainingCourse() throws EncryptedDocumentException, IOException {
		ImportParams params = ImportParams.create().term(TermSerivcelTest.TERM).classYear(classYearFilter);
		params.saveOnAllErrorTypes();
		File file1 = Files.of("/schedule-training-21.xls");
		File file2 = Files.of("/schedule-training-22.xls");
		ImportReport report1 = trainingScheduleImporter2.importFile(params.file(file1));
		ImportReport report2 = trainingScheduleImporter2.importFile(params.file(file2));
		System.out.println(report1);
		System.out.println(report2);
	}

	@Test
	public void testCountsGroupByWeek() {
		List<Map<String, Object>> counts = scheduleRepository.countsOfEachWeek(TermSerivcelTest.TERM.getId(),
				Schedule.COURSE_TYPE_NORMAL);
		for (Map<String, Object> count : counts) {
			System.out.print(count.get("classId"));
			System.out.print(" - ");
			System.out.print(count.get("courseCode"));
			System.out.print(" - ");
			System.out.print(count.get("weekno"));
			System.out.print(" - ");
			System.out.println(count.get("cnt"));
		}
		System.out.println("共【" + counts.size() + "】条记录");
	}

	@Test
	public void testSitesRecordOfUKName() {
		List<Site> au = siteRepository.findAllWithUniqueName();
		System.out.println("共【" + au.size() + "】条记录（Site－UK）");
		for (Site s : au) {
			System.out.println(s.getName());
		}
		List<Site> anu = siteRepository.findAllWithNotUniqueName();
		System.out.println("共【" + anu.size() + "】条记录（Site－NUK） - name:roomType:name4Training");
		for (Site s : anu) {
			System.out.print(s.getName());
			System.out.print(" : ");
			System.out.print(s.getRoomType());
			System.out.print(" : ");
			System.out.println(s.getName4Training());
		}
	}

}