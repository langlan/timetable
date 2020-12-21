package com.jytec.cs.excel;

import static com.jytec.cs.excel.TextParser.handleLogicalEmpty;
import static com.jytec.cs.excel.TextParser.handleMalFormedDegree;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.ModelMappingHelper.StagedCounts;
import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportReport;
import com.jytec.cs.excel.api.ImportReport.SheetImportReport;
import com.jytec.cs.excel.parse.AbstractParseResult;
import com.jytec.cs.excel.parse.Columns;
import com.jytec.cs.excel.parse.HeaderRowNotFountException;
import com.jytec.cs.service.AuthService;

/** dept, major, teacher, class, course, class-course */
@Service
public class ClassCourseImporter extends AbstractImporter {
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

	@Transactional
	@Override
	public ImportReport importFile(ImportParams params) throws EncryptedDocumentException, IOException {
		Assert.notNull(params.term, "参数学期不可为空！");

		return super.importFile(params);
	}

	@Override
	protected void doImport(Workbook wb, ImportContext context) {
		super.doImport(wb, context);
		if (!context.params.preview) {
			context.modelHelper.saveStaged();
			authService.assignIdcs();
			deptRepository.updateTypeOfNormal();
			deptRepository.updateTypeOfElse();
		}
	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		SheetImportReport rpt = context.report;

		Row headerRow = null;
		try {
			headerRow = cols.findHeaderRow(sheet, 0, 2);
		} catch (HeaderRowNotFountException e) {
			rpt.ignoredByReason(e.getMessage());
			log.info(e.getMessage());
			return;
		}

		BiConsumer<Row, ParseResult> rowProcessor = cols.buildRowProcessorByHeaderRow(headerRow);
		Random random = new Random();
		// StagingModels staging = context.getAttribute(StagingModels.class.getName(), StagingModels::new);
		StagingModels staging = new StagingModels();
		for (int rowIdx = headerRow.getRowNum() + 1; rowIdx < sheet.getLastRowNum(); rowIdx++) {
			Row dataRow = sheet.getRow(rowIdx);
			ParseResult r = new ParseResult();
			rowProcessor.accept(dataRow, r);
			r.row = dataRow;
			r.compose(random, context.params.term);
			staging.addIfValid(r);
			if (!r.valid) {
				rpt.logInvalidRow(dataRow.getRowNum(), r.reasons);
			}
			rpt.increseTotalRow();
		}
		StagedCounts old = context.modelHelper.getStatedCounts();
		staging.stage(context.modelHelper);
		StagedCounts n = context.modelHelper.getStatedCounts(), c = n.delta(old);

		log.info(rpt.log("新增系别数：" + c.depts + ", 共解析：" + staging.deptsIndexdByName.size()));
		log.info(rpt.log("新增专业数：" + c.majors + ", 共解析：" + staging.majorsIndexdByName.size()));
		log.info(rpt.log("新增班级数：" + c.classes + ", 共解析：" + staging.classesIndexedByName.size()));
		log.info(rpt.log("新增课程数：" + c.courses + ", 共解析：" + staging.coursesIndexedByCode.size()));
		log.info(rpt.log("新增教师数：" + c.teachers + ", 共解析：" + staging.countTeachers()));
		log.info(rpt.log(
				"新增班级选课数：" + c.classCourses + ", 共解析：" + staging.classCoursesIndexedByClassNameAndCourseCode.size()));
		rpt.rowsReady = c.classCourses;
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

	}

	static class StagingModels {
		List<ParseResult> rs = new LinkedList<>();
		Map<String, List<ParseResult>> deptsIndexdByName = new HashMap<>();
		Map<String, List<ParseResult>> majorsIndexdByName = new HashMap<>();
		Map<String, List<ParseResult>> classesIndexedByName = new HashMap<>();
		Map<String, List<ParseResult>> coursesIndexedByCode = new HashMap<>();
		Map<String, List<ParseResult>> teachersIndexedByName = new HashMap<>();
		Map<String, List<ParseResult>> classCoursesIndexedByClassNameAndCourseCode = new HashMap<>();
		Set<String> namesOfTeacherWithoutCode = new HashSet<>();

		public void addIfValid(ParseResult r) {
			if (r.valid)
				rs.add(r);
		}

		public void stage(ModelMappingHelper mhelper) {
			classCoursesIndexedByClassNameAndCourseCode = rs.stream()
					.collect(groupingBy(it -> it.classNameWithDegree + "-" + it.courseCode));
			Map<String, List<ParseResult>> duplicatedCC = classCoursesIndexedByClassNameAndCourseCode.entrySet()
					.stream().filter(it -> it.getValue().size() > 1)
					.collect(Collectors.toMap(it -> it.getKey(), it -> it.getValue()));
			if (duplicatedCC.size() > 0) {
				StringBuilder sb = new StringBuilder("存在重复选课数据：").append("\r\n");
				for (Entry<String, List<ParseResult>> e : duplicatedCC.entrySet()) {
					List<String> rrr = e.getValue().stream() //
							.map(it -> valueOf(it.row.getRowNum() + 1)).collect(toList());
					sb.append(e.getKey()).append(" - ");
					sb.append(Strings.join(rrr, ','));
				}
				throw new IllegalArgumentException(sb.toString());
			}
			deptsIndexdByName = rs.stream().collect(groupingBy(r -> r.deptName));
			majorsIndexdByName = rs.stream().collect(groupingBy(r -> r.majorNameWithDegree));
			classesIndexedByName = rs.stream().collect(groupingBy(r -> r.classNameWithDegree));
			coursesIndexedByCode = rs.stream().collect(groupingBy(r -> r.courseCode));
			teachersIndexedByName = rs.stream().collect(groupingBy(r -> r.teacher.getName()));
			rs.forEach(r -> {
				namesOfTeacherWithoutCode.addAll(r.otherTeacherNames);
				r.dept = mhelper.findDeptOrStageByName(r.dept);
				r.major = mhelper.findMajorOrStageByName(r.major, r.dept);
				r.cls = mhelper.findClassOrStageByName(r.cls, r.major);
				r.course = mhelper.findCourseOrStageByCourseCode(r.course);
				r.teacher = mhelper.findteacherOrStageByName(r.teacher);
				r.classCourse = mhelper.findClassCourseOrStageByClassNameAndCourseCode(r.classCourse, r.cls, r.course,
						r.teacher);
			});
			namesOfTeacherWithoutCode = namesOfTeacherWithoutCode.stream()
					.filter(t -> !teachersIndexedByName.containsKey(t)).collect(Collectors.toSet());

			namesOfTeacherWithoutCode.forEach(mhelper::stageTeacherWithoutCodeIfAbsent);
		}

		public int countTeachers() {
			return teachersIndexedByName.size() + namesOfTeacherWithoutCode.size();
		}

	}

}
