package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.LinkedList;
import java.util.List;

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
import com.jytec.cs.domain.Class;
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
		int imported = 0, totalRow = 0;
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

			List<ParseResult> rs = new LinkedList<>(); //TODO: check uk
			imported++;
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
				ParseResult r = generateParseResult(classNameWithDegree, scs, scheduledCell, timeInfo, term, mhelper);
				rs.add(r);
				mhelper.stageAll(r.schedules);
			}
		}
		log.info("成功导入【" + imported + "/" + totalRow + "】行，@【" + sheet.getSheetName() + "】");
		// Should it be called through other UI to keep data-import and date-build independent.
	}

	protected ParseResult generateParseResult(String classNameWithDegree, ScheduledCourse[] scs, Cell scheduledCell,
			TimeInfo timeInfo, Term term, ModelMappingHelper mhelper) {
		// deal with parsed schedule
		ParseResult r = new ParseResult(scheduledCell);
		for (ScheduledCourse sc : scs) {
			TimeRange times = sc.timeRange;
			byte weeknoStart = times.weeknoStart, weeknoEnd = times.weeknoEnd;
			byte timeStart = times.timeStart, timeEnd = times.timeEnd;
			ClassCourse classCourse = mhelper.findClassCourse(classNameWithDegree, sc.course, scheduledCell);
			Teacher teacher = mhelper.findTeacher(sc.teacher, classCourse, scheduledCell);
			Site site = mhelper.findSite(sc.site, scheduledCell);

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
				r.schedules.add(schedule);
				//mhelper.stage();
			}
		}
		return r;
	}

	protected static class ParseResult {
		final Cell scheduledCell;
		final List<Schedule> schedules = new LinkedList<>();

		public ParseResult(Cell scheduledCell) {
			this.scheduledCell = scheduledCell;
		}


	}

}
