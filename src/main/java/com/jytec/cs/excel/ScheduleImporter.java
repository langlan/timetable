package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;
import com.jytec.cs.excel.TitleInfo.TimeInfo;
import com.jytec.cs.excel.api.ImportReport.SheetImportReport;
import com.jytec.cs.excel.exceptions.ClassCourseNotFountException;
import com.jytec.cs.service.AuthService;

@Service
public class ScheduleImporter extends AbstractImporter {
	protected @Autowired ScheduleRepository scheduleRespository;
	private @Autowired AuthService authService;
	protected int defaultHeaderRowIndex = 1;

	@Override
	protected void doImport(Workbook wb, ImportContext context) {
		Assert.notNull(context.params.term, "参数学期不可为空！");
		Assert.isTrue(context.params.classYear > 0, "参数年级不可为空！");

		super.doImport(wb, context);
		log.info("准备导入 Schedule 记录数：" + context.modelHelper.newSchedules.size());
		if (!context.params.preview) {
			context.modelHelper.saveStaged();
			scheduleRespository.flush();
			authService.assignIdcs(); // for auto-created teachers
			scheduleRespository.updateDateByTerm(context.params.term.getId());
		}
	}

	@Override
	protected void after(Sheet sheet, ImportContext context) {
		super.after(sheet, context);
		Map<String, Set<Cell>> cce = context.modelHelper.classCourseNotFountExceptions;
		Map<String, Set<Cell>> tnm = context.modelHelper.teacherNotMatchExceptions;
		Map<String, Set<Cell>> tnf = context.modelHelper.teacherNotFoundExceptions;
		Map<String, Set<Cell>> snf = context.modelHelper.siteNotFoundExceptions;
		SheetImportReport rpt = context.report;
		log.info("准备导入【" + rpt.rowsReady + "/" + rpt.rowsTotal + "】行" + atLocaton(sheet));
		cce.forEach((k, v) -> rpt.log("无法找到班级选课记录 - " + k + "－"
				+ Strings.join(v.stream().map(it -> it.getAddress().toString()).collect(Collectors.toList()), ',')));
		tnm.forEach((k, v) -> rpt.log("教师与班级选课教师不匹配 - " + k + "－"
				+ Strings.join(v.stream().map(it -> it.getAddress().toString()).collect(Collectors.toList()), ',')));
		tnf.forEach((k, v) -> rpt.log("找不到教师记录 - " + k + "－"
				+ Strings.join(v.stream().map(it -> it.getAddress().toString()).collect(Collectors.toList()), ',')));
		snf.forEach((k, v) -> rpt.log("找不到上课场所记录 - " + k + "－"
				+ Strings.join(v.stream().map(it -> it.getAddress().toString()).collect(Collectors.toList()), ',')));
		cce.clear();
		tnm.clear();
		tnf.clear();
		snf.clear();
		if (context.params.saveOnClassCourseNotFound) {
			context.modelHelper.hasAnyClassCourseNotFountExceptions = false;
		}
		if (context.params.saveOnTeacherNotMatch) {
			context.modelHelper.hasAnyTeacherNotMatchExceptions = false;
		}
		if (context.params.saveOnTeacherNotFound) {
			context.modelHelper.hasAnyTeacherNotFoundExceptions = false;
		}
		if (context.params.saveOnSiteNotFound) {
			context.modelHelper.hasAnySiteNotFoundExceptions = false;
		}
	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		SheetImportReport rpt = context.report;
		Term term = context.params.term;
		int classYear = context.params.classYear;

		TitleInfo.searchAndValidateTerm(sheet, term);
		TitleInfo titleInfo = TitleInfo.search(sheet, defaultHeaderRowIndex);
		// TitleInfo titleInfo = TitleInfo.create(sheet, headerRowIndex);
		int dataFirstRowIndex = titleInfo.getFollowingDataRowIndex();
		String classYearFilter = Integer.toString(classYear % 2000);

		OverlappingChecker overlappingChecker = context.getAttribute(OverlappingChecker.class.getName(),
				OverlappingChecker::new);
		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row dataRow = sheet.getRow(rowIndex);
			Cell classCell = dataRow.getCell(titleInfo.classColIndex);
			// parse class
			String classNameWithDegree = TextParser.handleMalFormedDegree(cellString(classCell));
			if (classNameWithDegree.isEmpty()) {
				log.info("忽略无效数据行：班级列为空" + atLocaton(dataRow));
				continue;
			}
			if (!classNameWithDegree.contains(classYearFilter)) {
				log.info("忽略班级（非指定年级）：【" + classNameWithDegree + "】" + atLocaton(dataRow));
				continue;
			}
			rpt.rowsTotal++;
//			Class pc = TextParser.parseClass(classNameWithDegree);
//			if (scheduleRespository.countNonTrainingByClassAndTerm(pc.getName(), term.getId()) > 0) {
//				log.info("忽略班级（已有理论课排课记录）：【" + classNameWithDegree + "】" + atLocaton(dataRow));
//				continue;
//			}
			ModelMappingHelper mhelper = context.modelHelper;

			// loop each cell in data-row
			boolean hasAnyNonEmptyCell = false, importedAnyCell = false;
			for (int colIndex = 0; colIndex < dataRow.getLastCellNum(); colIndex++) {
				Cell scheduledCell = dataRow.getCell(colIndex);
				TimeInfo timeInfo;
				String scheduleText = cellString(scheduledCell);

				// Empty cell or not schedule column
				if ((timeInfo = (titleInfo.getTimeInfo(scheduledCell))) == null || scheduleText.isEmpty())
					continue;
				hasAnyNonEmptyCell = true;
				ScheduledCourse[] scs;

				// parse schedule
				try {
					scs = TextParser.parseSchedule(scheduleText);
				} catch (Exception e) {
					throw new IllegalStateException("课表数据格式有误【" + scheduleText + "】" + atLocaton(scheduledCell));
				}

				List<Schedule> schedules = generateParseResult(classNameWithDegree, scs, scheduledCell, timeInfo, term,
						mhelper);
				overlappingChecker.addAll(schedules, scheduledCell);
				importedAnyCell = !schedules.isEmpty();
				mhelper.stageAll(schedules);
			}
			if (!hasAnyNonEmptyCell) {// 所有排课列均为空。
				log.info(rpt.log("忽略班级（没有排课数据）：【" + classNameWithDegree + "】" + atLocaton(dataRow, false)));
			}
			if (importedAnyCell) {
				rpt.rowsReady++;
			}
		}
	}

	protected List<Schedule> generateParseResult(String classNameWithDegree, ScheduledCourse[] scs, Cell scheduledCell,
			TimeInfo timeInfo, Term term, ModelMappingHelper mhelper) {
		// deal with parsed schedule
		List<Schedule> schedules = new LinkedList<>();
		for (ScheduledCourse sc : scs) {
			TimeRange times = sc.timeRange;
			byte timeStart = times.timeStart, timeEnd = times.timeEnd;
			ClassCourse classCourse = null;
			try {
				classCourse = mhelper.findClassCourse(classNameWithDegree, sc.course, scheduledCell);
			} catch (ClassCourseNotFountException e) {
				// see #after & ModelMappingHelper#classCourseNotFountExceptions
				log.info("忽略班级选课（无法找到班级选课记录）" + classNameWithDegree + "-" + sc.course);
				continue;
			}
			Teacher teacher = mhelper.findTeacher(sc.teacher, classCourse, scheduledCell);
			Site site = mhelper.findSite(sc.site, scheduledCell);

			// fixed issue : same class may have multiple rows to correspond different week-no.
			// so we can not use term+class (use term+class+week-no instead) to determine duplicate import.
			if (!(this instanceof TrainingScheduleImporter)) {
				// int cnt = scheduleRespository.countTheoryByCCNamesAndTermWeeks(classNameWithDegree, sc.course,
				// term.getId(),weeknoStart, weeknoEnd);
				int cnt = mhelper.countTheoryCourseByWeek(classCourse, sc.timeRange.weeknos);
				if (cnt > 0) {
					log.info("忽略班级选课（对应周数内已有理论课排课记录）：【" + classNameWithDegree + "-" + sc.course + "】"
							+ atLocaton(scheduledCell));
					continue;
				}
			} else {
				int count = mhelper.countTrainingByWeek(classCourse, term.getId(), sc.timeRange.weeknos);
				if (count > 0) {
					log.info("忽略班级选课（对应周数内已有实训排课记录）：【" + classNameWithDegree + "】" + atLocaton(scheduledCell));
					continue;
				}
			}

			if (timeStart != timeInfo.timeStart || timeEnd != timeInfo.timeEnd) {
				throw new IllegalStateException("课程时间与表头时间不匹配：表头【" + timeStart + "," + timeEnd + "】，当前【"
						+ timeInfo.timeStart + "," + timeInfo.timeEnd + "】" + atLocaton(scheduledCell));
			}

			// try create schedule records and save.
			for (byte weekno : times.weeknos) {
				Schedule schedule = new Schedule();
				schedule.setCourseType(Schedule.COURSE_TYPE_NORMAL);
				schedule.setTerm(term);
				schedule.setTheClass(classCourse.getTheClass());
				schedule.setCourse(classCourse.getCourse());
				// schedule.setClassCourse(classCourse);
				schedule.setTeacher(teacher);
				schedule.setWeekno(weekno);
				schedule.setDayOfWeek(timeInfo.dayOfWeek);
				// schedule.setDate(date);
				schedule.setTimeStart(timeStart);
				schedule.setTimeEnd(timeEnd);
				schedule.recalcTimes();
				schedule.setSite(site);
				schedules.add(schedule);
				// mhelper.stage();
			}
		}
		return schedules;
	}

	protected boolean acceptRow(ImportContext context, RowParseContext rowContext) {

		return true;
	}

	static class RowParseContext {

	}

	/** For overlapping check. */
	public static class OverlappingChecker {
		Map<ClassCourseDay, List<LessonTime>> flatTree = new HashMap<>();

		public void addAll(List<Schedule> schedules, Cell cell) {
			for (Schedule s : schedules) {
				this.add(s, cell);
			}
		}

		public void add(Schedule schedule, Cell cell) {
			ClassCourseDay key = new ClassCourseDay(schedule);
			LessonTime ivalue = new LessonTime(schedule.getTimeStart(), schedule.getTimeEnd(), cell);
			List<LessonTime> lessonTimes = flatTree.get(key);
			if (lessonTimes == null) {
				lessonTimes = new LinkedList<>();
				flatTree.put(key, lessonTimes);
			} else {
				for (LessonTime lessonTime : lessonTimes) {
					if (lessonTime.overlappedWith(ivalue)) {
						throw new IllegalArgumentException(
								"课表中相同课程存在课时重叠：" + atLocaton(lessonTime.cell) + " VS " + atLocaton(ivalue.cell));
					}
				}
			}
			lessonTimes.add(ivalue);
		}

		static class LessonTime {
			final byte start, end;
			final Cell cell;

			public LessonTime(byte timeStart, byte timeEnd, Cell cell) {
				this.cell = cell;
				this.start = timeStart;
				this.end = timeEnd;
			}

			public boolean overlappedWith(LessonTime other) {
				return (other.start <= start && start <= other.end) || (start <= other.start && other.start <= end);
			}

		}

		static class ClassCourseDay {
			final Schedule schedule;

			public ClassCourseDay(Schedule schedule) {
				this.schedule = schedule;
			}

			@Override
			public int hashCode() {
				return Objects.hash(schedule.getTheClass().getId(), schedule.getCourse().getCode(),
						schedule.getWeekno(), schedule.getDayOfWeek());
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null || !(obj instanceof ClassCourseDay)) {
					return false;
				}
				ClassCourseDay c = (ClassCourseDay) obj;
				return schedule.getTheClass().getId() == c.schedule.getTheClass().getId()
						&& schedule.getCourse().getCode().equals(c.schedule.getCourse().getCode())
						&& schedule.getWeekno() == c.schedule.getWeekno()
						&& schedule.getDayOfWeek() == c.schedule.getDayOfWeek();
			}
		}

	}

}
