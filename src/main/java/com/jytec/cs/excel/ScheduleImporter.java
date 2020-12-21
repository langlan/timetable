package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;
import com.jytec.cs.excel.TitleInfo.TimeInfo;
import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportReport;
import com.jytec.cs.excel.api.ImportReport.SheetImportReport;
import com.jytec.cs.excel.schedule.Keys;
import com.jytec.cs.excel.schedule.Keys.CourseSiteTime;
import com.jytec.cs.service.AuthService;

@Service
public class ScheduleImporter extends AbstractImporter {
	protected @Autowired ScheduleRepository scheduleRespository;
	private @Autowired AuthService authService;
	protected int defaultHeaderRowIndex = 1;
	
	@Transactional
	@Override
	public ImportReport importFile(ImportParams params) throws EncryptedDocumentException, IOException {
		Assert.notNull(params.term, "参数学期不可为空！");
		Assert.isTrue(params.classYear > 0, "参数年级不可为空！");
		
		return super.importFile(params);
	}

	@Override
	protected void doImport(Workbook wb, ImportContext context) {
		super.doImport(wb, context);
		if (!(this instanceof TrainingScheduleImporter)) {
			this.mergeClasses(context.modelHelper.newSchedules);
		}

		log.info("准备导入 Schedule 记录数：" + context.modelHelper.newSchedules.size());
		if (!context.params.preview) {
			context.modelHelper.saveStaged();
			scheduleRespository.flush();
			authService.assignIdcs(); // for auto-created teachers
			scheduleRespository.updateDateByTerm(context.params.term.getId());
		}
	}

	private void mergeClasses(List<Schedule> schedules) {
		Map<Boolean, List<Schedule>> nonEmptySiteOrNot = schedules.stream()
				.collect(Collectors.groupingBy(it -> it.getSite() != null));
		List<Schedule> withoutSite = nonEmptySiteOrNot.get(false);
		List<Schedule> withSite = nonEmptySiteOrNot.get(true);
		schedules.clear();
		if (withoutSite != null) {
			schedules.addAll(withoutSite);
		}
		if (withSite == null) {
			return;
		}
		// 理论课表每两小节一个单元格，故合并时无须考虑拆份，直接按时间相同的分组合并即可
		// 注意后续课表可能单元格合并的情况（因为从其它系统导出，可能性不大，但出现时应在生成处或此处更改逻辑）。
		Map<CourseSiteTime, List<Schedule>> grouped = nonEmptySiteOrNot.get(true).stream()
				.collect(Collectors.groupingBy(Keys.CourseSiteTime::new));
		for (List<Schedule> beMerged : grouped.values()) {
			Schedule main = null;
			for (Schedule e : beMerged) {
				if (main == null) {
					main = e;
				} else {
					main.getClasses().addAll(e.getClasses());
					main.setClassCount((byte) main.getClasses().size());
					main.recalcRedundant();
				}
			}
			schedules.add(main);
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

		ModelMappingHelper mhelper = context.modelHelper;
		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row dataRow = sheet.getRow(rowIndex);
			Cell classCell = dataRow.getCell(titleInfo.classColIndex);

			// accept row ?
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

			// loop each schedule-cell in data-row
			boolean hasAnyNonEmptyCell = false, anyCellStaged = false;
			// List<ScheduledCourse> vscs = new LinkedList<>();
			for (int colIndex = 0; colIndex < dataRow.getLastCellNum(); colIndex++) {
				Cell scheduledCell = dataRow.getCell(colIndex);
				TimeInfo timeInfo;
				String scheduleText = cellString(scheduledCell);

				// Empty cell or not schedule column
				if ((timeInfo = (titleInfo.getTimeInfo(scheduledCell))) == null || scheduleText.isEmpty())
					continue;
				hasAnyNonEmptyCell = true;
				ScheduledCourse[] scs = TextParser.parseSchedule(scheduleText);
				mhelper.resolve(scs, new String[] { classNameWithDegree }, timeInfo.dayOfWeek, scheduledCell);

				List<Schedule> schedules = generate(scs, scheduledCell, timeInfo, term, mhelper);
				anyCellStaged |= !schedules.isEmpty();
				mhelper.stageAll(schedules);
			}
			if (!hasAnyNonEmptyCell) {// 所有排课列均为空。
				log.info(rpt.log("忽略班级（没有排课数据）：【" + classNameWithDegree + "】" + atLocaton(dataRow, false)));
			} else if (anyCellStaged) {
				rpt.rowsReady++;
			}
		}
	}

	protected List<Schedule> generate(ScheduledCourse[] scs, Cell scheduledCell, TimeInfo timeInfo, Term term,
			ModelMappingHelper mhelper) {
		// deal with parsed schedule
		List<Schedule> schedules = new LinkedList<>();
		for (ScheduledCourse sc : scs) {
			if (!sc.resolveSuccess) {
				continue;
			}
			if (!acceptScheduleCell(sc, scheduledCell, mhelper)) {
				continue;
			}

			TimeRange times = sc.timeRange;
			byte timeStart = times.timeStart, timeEnd = times.timeEnd;
			Course course = sc.getCourse();
			for (byte weekno : times.weeknos) {
				Schedule schedule = new Schedule();
				schedule.setCourseType(Schedule.COURSE_TYPE_NORMAL);
				schedule.setTerm(term);
				schedule.setWeekno(weekno);
				schedule.setDayOfWeek(timeInfo.dayOfWeek);
				// schedule.setDate(date);
				schedule.setTimeStart(timeStart);
				schedule.setTimeEnd(timeEnd);
				// model props and count.
				schedule.setCourse(course);
				schedule.setSite(sc.site);
				schedule.setClasses(sc.getClasses());
				schedule.setTeachers(sc.teachers);
				schedule.setClassCount((byte) sc.getClasses().size());
				schedule.setTeacherCount((byte) sc.teachers.size());
				schedule.recalcRedundant();
				schedules.add(schedule);
			}
		}
		return schedules;
	}

	protected boolean acceptScheduleCell(ScheduledCourse sc, Cell scheduledCell, ModelMappingHelper mhelper) {
		ClassCourse classCourse = sc.classCourses.get(0); // for theory, only one.
		// fixed issue : same class may have multiple rows to correspond different week-no.
		// so we can not use term+class (use term+class+week-no instead) to determine duplicate import.
		int cnt = mhelper.countTheoryScheduleByWeek(classCourse, sc.timeRange.weeknos);
		if (cnt > 0) {
			log.info("忽略班级选课（对应周数内已有理论课排课记录）：【" + classCourse.getTheClass().getName() + "-" + sc.courseName + "】"
					+ atLocaton(scheduledCell));
			return false;
		}

//		if (timeStart != timeInfo.timeStart || timeEnd != timeInfo.timeEnd) {
//			throw new IllegalStateException("课程时间与表头时间不匹配：表头【" + timeStart + "," + timeEnd + "】，当前【"
//					+ timeInfo.timeStart + "," + timeInfo.timeEnd + "】" + atLocaton(scheduledCell));
//		}
		return true;
	}

}
