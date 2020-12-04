package com.jytec.cs.excel;

import static com.jytec.cs.excel.TextParser.assertEquals;
import static com.jytec.cs.excel.TextParser.cellString;
import static com.jytec.cs.excel.TextParser.handleLogicalEmpty;
import static com.jytec.cs.excel.TextParser.handleMalFormedDegree;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.ClassCourseRepository;
import com.jytec.cs.dao.ClassRepository;
import com.jytec.cs.dao.CourseRepository;
import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.dao.MajorRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.service.AuthService;
import com.jytec.cs.service.AutoCreateService;

@Service
public class ClassCourseImporter {
	private static final Log log = LogFactory.getLog(ClassCourseImporter.class);
	private @Autowired DeptRepository deptRepository;
	private @Autowired MajorRepository majorRepository;
	// private @Autowired TeacherRepository teacherRepository;
	private @Autowired ClassRepository classRepository;
	private @Autowired CourseRepository courseRepository;
	private @Autowired ClassCourseRepository classCourseRepository;
	private @Autowired AutoCreateService autoCreateService;
	private @Autowired AuthService authService;

	@Transactional
	public void importFile(Term term, File file) throws EncryptedDocumentException, IOException {
		Workbook wb = WorkbookFactory.create(file, null, true);
		doImport(term, wb);
		wb.close();
	}

	protected void doImport(Term term, Workbook wb) {
		// assertEquals(1, wb.getNumberOfSheets());

		Sheet sheet = wb.getSheetAt(0);

		Row titleRow = sheet.getRow(0);
		assertEquals("学生学院", cellString(titleRow.getCell(18)));

		assertEquals("专业名称", cellString(titleRow.getCell(0))); // A-0
		assertEquals("班级名称", cellString(titleRow.getCell(1))); // B-1
		assertEquals("课程代码", cellString(titleRow.getCell(2))); // C-2
		assertEquals("课程名称", cellString(titleRow.getCell(3))); // D-3
		assertEquals("课程性质", cellString(titleRow.getCell(10))); // K-10-课程性质：公共基础|专业技能|专业技能（选）|实习环节|无
		assertEquals("课程类别", cellString(titleRow.getCell(11))); // L-11-课程类别：必修课|选修课|限定选修课|无
		assertEquals("考核方式", cellString(titleRow.getCell(12))); // M-12-考核方式：考试|考查|无
		assertEquals("考核方式", cellString(titleRow.getCell(12))); // M-12-考核方式：考试|考查|无
		assertEquals("实验课是否跟理论", cellString(titleRow.getCell(13))); // N-13
		// assertEquals("公用资源", cellString(titleRow.getCell(14))); // O-14－公用资源：否
		assertEquals("场地要求", cellString(titleRow.getCell(15))); // P-15
		assertEquals("教师职工号", cellString(titleRow.getCell(4))); // E-4-教师职工号
		assertEquals("教师姓名", cellString(titleRow.getCell(5))); // F-5-教师姓名

		// Although enable caching, it is id-based, so we use logical-key witch is business-columns-based
		boolean cacheReady = false;
		Set<String> classCourseKeys = new HashSet<>();
		Map<String, Dept> depts = new HashMap<>();
		Map<String, Major> majors = new HashMap<>();
		Map<String, Class> classes = new HashMap<>();
		classCourseRepository.findAllLogicKeyByTerm(term.getTermYear(), term.getTermMonth()).forEach(it -> {
//			String classKey = ((Class) it[0]).getName() + "[" + ((Class) it[0]).getDegree() + "]";
//			String courseCode = it[1].toString();
			if (!classCourseKeys.add(it.toString()))
				throw new IllegalStateException(
						"现存数据重复：班级选课表（ClassCourse）：" + term.getTermYear() + "-" + term.getTermMonth() + "-" + it);
		});
		int count = 0;

		Random random = new Random();
		Set<String> otherTeacherNames = new HashSet<>();
		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			String deptName = cellString(row.getCell(18));
			String majorNameWithDegree = cellString(row.getCell(0));
			String classNameWithDegree = cellString(row.getCell(1));
			String courseCode = cellString(row.getCell(2));
			String courseName = cellString(row.getCell(3));
			String courseCate = cellString(row.getCell(10));
			String courseStyle = cellString(row.getCell(11));
			String courseExamineMethod = cellString(row.getCell(12));
			String courseLabByTheory = cellString(row.getCell(13));
			String courseLocationType = cellString(row.getCell(15));
			String teacherCode = cellString(row.getCell(4));
			String _teacherNames = cellString(row.getCell(5));

			String classCourseKey = classNameWithDegree + "-" + courseCode;
			String deptKey = deptName;
			String majorKey = majorNameWithDegree = handleMalFormedDegree(majorNameWithDegree);
			String classKey = classNameWithDegree = handleMalFormedDegree(classNameWithDegree);
			// seems like no malformed with className happens, but still...

			if (classCourseKeys.contains(classCourseKey)) {
				continue; // ignore exists;
			}

			if (!cacheReady) {
				cacheReady = true;
				deptRepository.findAll().forEach(dept -> depts.put(dept.getName(), dept));
				// majors = StreamSupport.stream(majorRepository.findAll().spliterator(), false).collect(
				// Collectors.toMap(major -> major.getName() + "[" + major.getDegree() + "]", major -> major));
				majorRepository.findAll()
						.forEach(major -> majors.put(major.getName() /* +"[" + major.getDegree() + "]" */, major));
				classRepository.findAll()
						.forEach(clas -> classes.put(clas.getName() /* + "[" + clas.getDegree() + "]" */, clas));
			}

			// find or create department.
			Dept dept = depts.get(deptKey); // find unique by name.
			if (dept == null) {
				dept = new Dept();
				dept.setName(deptName);
				deptRepository.save(dept);
				depts.put(deptKey, dept);
			}

			// find or create major, class
			Major major = majors.get(majorKey);
			Class pclass = TextParser.parseClass(classNameWithDegree);
			Class theClass = classes.get(classKey); // find unique by name and degree.
			if (major == null) {
				major = pclass.getMajor();
				TextParser.parseMajor(majorKey, major);
				major.setDept(dept);
				majorRepository.save(major);
				majors.put(majorKey, major);
			} else {
				pclass.setMajor(major);
			}

			if (theClass == null) {
				theClass = pclass;
				theClass.setSize(30 + random.nextInt(20));
				classes.put(classKey, theClass);
				classRepository.save(theClass);
			}

			// find or create course.
			Course course = courseRepository.findById(courseCode).orElseGet(() -> {
				Course _course = new Course();
				_course.setCode(courseCode);
				_course.setName(courseName);
				_course.setCate(handleLogicalEmpty(courseCate));
				_course.setStyle(handleLogicalEmpty(courseStyle));
				_course.setExamineMethod(handleLogicalEmpty(courseExamineMethod));
				_course.setLabByTheory("是".equals(courseLabByTheory));
				_course.setLocationType(courseLocationType);
				courseRepository.save(_course);
				return _course;
			});

			// find or create teacher
			String[] teacherNames = _teacherNames.split("/");
			Teacher teacher = autoCreateService.findTeacherByNameOrCreateWithCode(teacherNames[0], teacherCode);
			// auto-create later if necessary
			for (int ti = 1; ti < teacherNames.length; ti++) {
				otherTeacherNames.add(teacherNames[ti]);
			}

			classCourseRepository.flush();
			// create class-course.
			ClassCourse classCourse = new ClassCourse();
			classCourse.setCourse(course);
			classCourse.setTheClass(theClass);
			classCourse.setTeacher(teacher);
			classCourse.setTeacherNames(_teacherNames);
			classCourse.setTermYear(term.getTermYear());
			classCourse.setTermMonth(term.getTermMonth());
			classCourseRepository.save(classCourse);
			count++;
		}
		otherTeacherNames.forEach(it -> autoCreateService.findTeacherByNameOrCreateWithAutoCode(it));
		authService.assignIdcs();
		log.info("导入班级选课记录共【" + count + "/" + sheet.getLastRowNum() + "】");
	}

}
