package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.jytec.cs.service.AuthService;

@Service
public class ScheduleImporter extends AbstractImporter {
	private final Log log = LogFactory.getLog(ScheduleImporter.class);
	protected @Autowired ScheduleRepository scheduleRespository;
	private @Autowired AuthService authService;

	@Override
	protected void doImport(Workbook wb, ImportContext context) {
		super.doImport(wb, context);
		if (!context.params.preview) {
			context.modelHelper.saveStaged();
			authService.assignIdcs();
			scheduleRespository.updateDateByTerm(context.params.term.getId());
		}
	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		Term term = context.params.term;
		int classYear = context.params.classYear;
		Assert.notNull(term, "参数学期不可为空！");
		Assert.notNull(classYear, "参数年级不可为空！");
		int headerRowIndex = 1, dataFirstRowIndex = headerRowIndex + 1;

		TitleInfo.searchAndValidateTerm(sheet, term);
		TitleInfo titleInfo = TitleInfo.search(sheet, 1);
		// TitleInfo titleInfo = TitleInfo.create(sheet, headerRowIndex);
		int classColIndex = titleInfo.classColIndex;
		String classYearFilter = Integer.toString(classYear % 2000);
		int importedRows = 0, totalRow = 0;

		OverlappingChecker overlappingChecker = context.getAttribute(OverlappingChecker.class.getName(), OverlappingChecker::new);
		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row dataRow = sheet.getRow(rowIndex);
			Cell cell = dataRow.getCell(classColIndex);
			totalRow++;
			// parse class
			String classNameWithDegree = TextParser.handleMalFormedDegree(cellString(cell));
			if (classNameWithDegree.isEmpty()) {
				log.info("忽略无效数据行：班级列为空" + atLocaton(dataRow));
				continue;
			}
			if (!classNameWithDegree.contains(classYearFilter)) {
				log.info("忽略班级（非指定年级）：【" + classNameWithDegree + "】" + atLocaton(dataRow));
				continue;
			}

//			Class pc = TextParser.parseClass(classNameWithDegree);
//			if (scheduleRespository.countNonTrainingByClassAndTerm(pc.getName(), term.getId()) > 0) {
//				log.info("忽略班级（已有理论课排课记录）：【" + classNameWithDegree + "】" + atLocaton(dataRow));
//				continue;
//			}
			ModelMappingHelper mhelper = context.modelHelper;

			boolean importedAnyRow = false;
			for (int colIndex = 0; colIndex < dataRow.getLastCellNum(); colIndex++) {
				Cell scheduledCell = dataRow.getCell(colIndex);
				TimeInfo timeInfo;
				String scheduleText = cellString(scheduledCell);

				// Empty cell or not schedule column
				if ((timeInfo = (titleInfo.getTimeInfo(scheduledCell))) == null || scheduleText.isEmpty())
					continue;

				ScheduledCourse[] scs;

				// parse schedule
				try {
					scs = TextParser.parseSchedule(scheduleText);
				} catch (Exception e) {
					throw new IllegalStateException("课表数据格式有误【" + scheduleText + "】" + atLocaton(scheduledCell));
				}
				List<Schedule> schedules = generateParseResult(classNameWithDegree, scs, scheduledCell, timeInfo, term, mhelper);
				overlappingChecker.addAll(schedules, scheduledCell);
				importedAnyRow = !schedules.isEmpty();
				mhelper.stageAll(schedules);
			}
			importedRows = importedRows + (importedAnyRow ? 1 : 0);
		}
		log.info("成功导入【" + importedRows + "/" + totalRow + "】行，@【" + sheet.getSheetName() + "】");
		// Should it be called through other UI to keep data-import and date-build independent.
	}

	protected List<Schedule> generateParseResult(String classNameWithDegree, ScheduledCourse[] scs, Cell scheduledCell,
			TimeInfo timeInfo, Term term, ModelMappingHelper mhelper) {
		// deal with parsed schedule
		List<Schedule> schedules = new LinkedList<>();
		for (ScheduledCourse sc : scs) {
			TimeRange times = sc.timeRange;
			byte weeknoStart = times.weeknoStart, weeknoEnd = times.weeknoEnd;
			byte timeStart = times.timeStart, timeEnd = times.timeEnd;
			ClassCourse classCourse = mhelper.findClassCourse(classNameWithDegree, sc.course, scheduledCell);
			Teacher teacher = mhelper.findTeacher(sc.teacher, classCourse, scheduledCell);
			Site site = mhelper.findSite(sc.site, scheduledCell);

			if (!(this instanceof TrainingScheduleImporter))
				if (scheduleRespository.countTheoryByCCNamesAndTermWeeks(classNameWithDegree, sc.course, term.getId(),
						weeknoStart, weeknoEnd) > 0) {
					log.info("忽略班级选课（对应周数内已有理论课排课记录）：【" + classNameWithDegree + "-" + sc.course + "】"
							+ atLocaton(scheduledCell));
					continue;
				}

			if (timeStart != timeInfo.timeStart || timeEnd != timeInfo.timeEnd) {
				throw new IllegalStateException("课程时间与表头时间不匹配：表头【" + timeStart + "," + timeEnd + "】，当前【"
						+ timeInfo.timeStart + "," + timeInfo.timeEnd + "】" + atLocaton(scheduledCell));
			}

			// try create schedule records and save.
			for (byte weekno = weeknoStart; weekno <= weeknoEnd; weekno++) {
				if (times.oddWeekOnly != null && ((weekno % 2 == 0) == times.oddWeekOnly)) {
					log.debug("仅" + (times.oddWeekOnly ? "单" : "双") + "数周，排除：" + weekno + atLocaton(scheduledCell));
					continue; // exclude even when odd-only, or exclude odd when even-only
				}
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
				schedule.setSite(site);
				schedules.add(schedule);
				// mhelper.stage();
			}
		}
		return schedules;
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
				return (other.start <= start && start <= other.end) || other.overlappedWith(this);
			}

		}

		class ClassCourseDay {
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
				if (obj == null || obj instanceof ClassCourseDay) {
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
