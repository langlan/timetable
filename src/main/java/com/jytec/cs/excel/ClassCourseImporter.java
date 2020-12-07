package com.jytec.cs.excel;

import static com.jytec.cs.excel.TextParser.handleLogicalEmpty;
import static com.jytec.cs.excel.TextParser.handleMalFormedDegree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.api.ImportPreview.SheetImportPreview;
import com.jytec.cs.excel.parse.AbstractParseResult;
import com.jytec.cs.excel.parse.Columns;
import com.jytec.cs.excel.parse.HeaderRowNotFountException;
import com.jytec.cs.service.AuthService;

/** dept, major, teacher, class, course, class-course */
@Service
public class ClassCourseImporter extends AbstractImporter {
	private static final Log log = LogFactory.getLog(ClassCourseImporter.class);
	private @Autowired DeptRepository deptRepository;
	private @Autowired AuthService authService;
	private Columns<ParseResult> cols = new Columns<>();

	public ClassCourseImporter() {
		cols.scol("学生学院", (v, r) -> r.deptName = v);

		cols.scol("专业名称", (v, r) -> r.majorNameWithDegree = v); // A-0
		cols.scol("班级名称", (v, r) -> r.classNameWithDegree = v); // B-1
		cols.scol("课程代码", (v, r) -> r.courseCode = v); // C-2
		cols.scol("课程名称", (v, r) -> r.courseName = v); // D-3
		cols.scol("课程性质", (v, r) -> r.courseCate = v); // K-10-课程性质：公共基础|专业技能|专业技能（选）|实习环节|无 courseCate
		cols.scol("课程类别", (v, r) -> r.courseStyle = v); // L-11-课程类别：必修课|选修课|限定选修课|无 courseStyle
		cols.scol("考核方式", (v, r) -> r.courseExamineMethod = v); // M-12-考核方式：考试|考查|无
		cols.scol("实验课是否跟理论", (v, r) -> r.courseLabByTheory = v); // N-13
		// cols.scol("公用资源", (v, r)-> r.deptName = v); // O-14－公用资源：否
		cols.scol("场地要求", (v, r) -> r.courseLocationType = v); // P-15
		cols.scol("教师职工号", (v, r) -> r.teacherCode = v); // E-4-教师职工号
		cols.scol("教师姓名", (v, r) -> r.teacherNames = v); // F-5-教师姓名
	}

	@Override
	protected void doImport(Workbook wb, ImportContext context) {
		super.doImport(wb, context);
		if (context.params.ignorePreview) {
			context.modelHelper.proceedSaving();
			authService.assignIdcs();
			deptRepository.updateTypeOfNormal();
			deptRepository.updateTypeOfElse();
		}
	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		SheetImportPreview sheetPreview = new SheetImportPreview();
		context.preview.append(sheetPreview);

		Row headerRow = null;
		try {
			headerRow = cols.findHeaderRow(sheet, 0, 2);
		} catch (HeaderRowNotFountException e) {
			sheetPreview.ignoredByReason(e.getMessage());
			log.info(e.getMessage());
			return;
		}

		BiConsumer<Row, ParseResult> rowProcessor = cols.buildRowProcessorByHeaderRow(headerRow);
		Random random = new Random();
		for (int rowIdx = headerRow.getRowNum() + 1; rowIdx < sheet.getLastRowNum(); rowIdx++) {
			Row dataRow = sheet.getRow(rowIdx);
			ParseResult r = new ParseResult();
			rowProcessor.accept(dataRow, r);
			r.compose(random, context.params.term);
			r.stageIfValid(context.modelHelper);
			if (!r.valid) {
				sheetPreview.invalidRow(dataRow.getRowNum(), r.reasons);
			}
			sheetPreview.increseValidRow();
		}
		log.debug("新增系别数" + context.modelHelper.newDepts.size());
		log.debug("新增专业数" + context.modelHelper.newMajors.size());
		log.debug("新增班级数" + context.modelHelper.newClasses.size());
		log.debug("新增教师数" + context.modelHelper.newTeachers.size());
		log.debug("新增教师（无职工号）数" + context.modelHelper.newTeachersWithoutCode.size());
		log.debug("新增班级选课数" + context.modelHelper.newClassCourses.size());
	}

	static class ParseResult extends AbstractParseResult {
		public String deptName;
		public String majorNameWithDegree;
		public String classNameWithDegree;
		public String courseCode, courseName, courseCate, courseStyle, courseExamineMethod;
		public String courseLabByTheory, courseLocationType, teacherCode;
		public String teacherNames;

		public Dept dept;
		public Major major;
		public Class cls;
		public Course course;
		public Teacher teacher;
		public ClassCourse classCourse;
		public List<String> otherTeacherNames;

		void compose(Random random, Term term) {
			majorNameWithDegree = handleMalFormedDegree(majorNameWithDegree);
			classNameWithDegree = handleMalFormedDegree(classNameWithDegree);

			proceedIfPresent(() -> {
				dept = TextParser.parseDept(deptName);
				cls = TextParser.parseClass(classNameWithDegree);
				major = TextParser.parseMajor(majorNameWithDegree);
			}, "系别列、专业列、班级列不可为空", deptName, classNameWithDegree, majorNameWithDegree);

			proceedIfPresent(() -> {
				major.setShortName(cls.getMajor().getShortName());
				cls.setMajor(major);
				cls.setSize(30 + random.nextInt(20));
			}, "专业列、班级列格式有误（名称[教育程度]）", deptName, classNameWithDegree, majorNameWithDegree);

			proceedIfPresent(() -> {
				String[] teacherNamesArray = teacherNames.split("/");
				teacher = new Teacher();
				teacher.setName(teacherNamesArray[0]);
				teacher.setCode(teacherCode);
				otherTeacherNames = teacherNamesArray.length > 1
						? Arrays.asList(teacherNamesArray).subList(1, teacherNamesArray.length)
						: Collections.emptyList();
			}, "教师名称、或职工号不可为空", teacherNames, teacherCode);

			proceedIfPresent(() -> {
				course = new Course();
				course.setCode(courseCode);
				course.setName(courseName);
				course.setCate(handleLogicalEmpty(courseCate));
				course.setStyle(handleLogicalEmpty(courseStyle));
				course.setExamineMethod(handleLogicalEmpty(courseExamineMethod));
				course.setLabByTheory("是".equals(courseLabByTheory));
				course.setLocationType(courseLocationType);
			}, "课程名、课程编号不可为空", courseCode, courseName);

			if (this.valid) {
				classCourse = new ClassCourse();
				classCourse.setTeacherNames(teacherNames);
				classCourse.setTerm(term);
			}
		}

		void stageIfValid(ModelMappingHelper mhelper) {
			if (this.valid) {
				dept = mhelper.findDeptByName(dept);
				major = mhelper.findMajorByName(major, dept);
				cls = mhelper.findClassByName(cls, major);
				course = mhelper.findCourseByCourseCode(course);
				teacher = mhelper.findteacherByName(teacher);
				classCourse = mhelper.findClassCourseByClassNameAndCourseCode(classCourse, cls, course, teacher);
				mhelper.otherTeachers(this.otherTeacherNames);
			}
		}

	}

}
