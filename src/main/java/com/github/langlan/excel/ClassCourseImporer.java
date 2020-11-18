package com.github.langlan.excel;

import static com.github.langlan.excel.TextParser.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.langlan.dao.ClassCourseRepository;
import com.github.langlan.dao.ClassRepository;
import com.github.langlan.dao.CourseRepository;
import com.github.langlan.dao.DeptRepository;
import com.github.langlan.dao.MajorRepository;
import com.github.langlan.dao.TeacherRepository;
import com.github.langlan.domain.Class;
import com.github.langlan.domain.ClassCourse;
import com.github.langlan.domain.Course;
import com.github.langlan.domain.Dept;
import com.github.langlan.domain.Major;
import com.github.langlan.domain.Teacher;
import com.github.langlan.domain.Term;

@Service
public class ClassCourseImporer {
	private @Autowired DeptRepository deptRepository;
	private @Autowired MajorRepository majorRepository;
	private @Autowired TeacherRepository teacherRepository;
	private @Autowired ClassRepository classRepository;
	private @Autowired CourseRepository courseRepository;
	private @Autowired ClassCourseRepository classCourseRepository;

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
		assertEquals("学生学院", titleRow.getCell(18).toString());

		assertEquals("专业名称", titleRow.getCell(0).toString()); // A-0
		assertEquals("班级名称", titleRow.getCell(1).toString()); // B-1
		assertEquals("课程代码", titleRow.getCell(2).toString()); // C-2
		assertEquals("课程名称", titleRow.getCell(3).toString()); // D-3
		assertEquals("课程性质", titleRow.getCell(10).toString()); // K-10-课程性质：公共基础|专业技能|专业技能（选）|实习环节|无
		assertEquals("课程类别", titleRow.getCell(11).toString()); // L-11-课程类别：必修课|选修课|限定选修课|无
		assertEquals("考核方式", titleRow.getCell(12).toString()); // M-12-考核方式：考试|考查|无
		assertEquals("考核方式", titleRow.getCell(12).toString()); // M-12-考核方式：考试|考查|无
		assertEquals("实验课是否跟理论", titleRow.getCell(13).toString()); // N-13
		// assertEquals("公用资源", titleRow.getCell(14).toString()); // O-14－公用资源：否
		assertEquals("场地要求", titleRow.getCell(15).toString()); // P-15
		assertEquals("教师职工号", titleRow.getCell(4).toString()); // E-4-教师职工号
		assertEquals("教师姓名", titleRow.getCell(5).toString()); // F-5-教师姓名

		// Although enable cache, it is id-based, so we use business-column-based
		Set<String> classCourseKeys = new HashSet<>();
		Map<String, Dept> depts = new HashMap<>();
		Map<String, Major> majors = new HashMap<>();
		Map<String, Class> classes = new HashMap<>();
		classCourseRepository.findClassAndCourseCodeByTerm(term.getTermYear(), term.getTermMonth()).forEach(it -> {
			String classKey = ((Class) it[0]).getName() + "[" + ((Class) it[0]).getDegree() + "]";
			String courseCode = it[1].toString();
			if (!classCourseKeys.add(classKey + "-" + courseCode))
				throw new IllegalStateException("现存数据重复：班级选课表（ClassCourse）：" + term.getTermYear() + "-"
						+ term.getTermMonth() + "-" + classKey + "-" + courseCode);
		});

		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			String deptName = row.getCell(18).toString();
			String majorNameWithDegree = row.getCell(0).toString();
			String classNameWithDegree = row.getCell(1).toString();
			String courseCode = row.getCell(2).toString();
			String courseName = row.getCell(3).toString();
			String courseCate = row.getCell(10).toString();
			String courseStyle = row.getCell(11).toString();
			String courseExamineMethod = row.getCell(12).toString();
			String courseLabByTheory = row.getCell(13).toString();
			String courseLocationType = row.getCell(15).toString();
			String teacherCode = row.getCell(4).toString();
			String teacherName = row.getCell(5).toString();

			String classCourseKey = classNameWithDegree + "-" + courseCode;
			String deptKey = deptName;
			String majorKey = majorNameWithDegree;
			String classKey = classNameWithDegree;

			if (classCourseKeys.contains(classCourseKey)) {
				continue; // ignore exists;
			}

			if (deptRepository == null) {
				deptRepository.findAll().forEach(dept -> depts.put(dept.getName(), dept));
				majorRepository.findAll()
						.forEach(major -> majors.put(major.getName() + "[" + major.getDegree() + "]", major));
				classRepository.findAll()
						.forEach(clas -> classes.put(clas.getName() + "[" + clas.getDegree() + "]", clas));
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
			Class _class = TextParser.parseClass(classNameWithDegree);
			Class theClass = classes.get(classKey); // find unique by name and degree.
			if (major == null) {
				major = _class.getMajor();
				TextParser.parseMajor(majorKey, major);
				major.setDept(dept);
				majorRepository.save(major);
				majors.put(majorKey, major);
			} else {
				_class.setMajor(major);
			}

			if (theClass == null) {
				theClass = _class;
				classes.put(classKey, theClass);
				classRepository.save(theClass);
			}

			// find or create course.
			Course course = courseRepository.findById(courseCode).orElseGet(() -> {
				Course _course = new Course();
				_course.setCode(courseCode);
				_course.setName(courseName);
				_course.setCate(courseCate);
				_course.setStyle(courseStyle);
				_course.setExamineMethod(courseExamineMethod);
				_course.setLabByTheory("是".equals(courseLabByTheory));
				_course.setLocationType(courseLocationType);
				courseRepository.save(_course);
				return _course;
			});

			// find or create teacher
			Teacher teacher = teacherRepository.findById(teacherCode).orElseGet(() -> {
				Teacher _teacher = new Teacher();
				_teacher.setCode(teacherCode);
				_teacher.setName(teacherName);
				teacherRepository.save(_teacher);
				return _teacher;
			});

			classCourseRepository.flush();
			// create class-course.
			ClassCourse classCourse = new ClassCourse();
			classCourse.setCourse(course);
			classCourse.setTheClass(theClass);
			classCourse.setTeacher(teacher);
			classCourse.setTermYear(term.getTermYear());
			classCourse.setTermMonth(term.getTermMonth());
			classCourseRepository.save(classCourse);
		}
	}

}
