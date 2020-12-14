package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;
import com.jytec.cs.excel.TitleInfo.TimeInfo;
import com.jytec.cs.excel.api.ImportReport.SheetImportReport;
import com.jytec.cs.excel.parse.MergingAreas;
import com.jytec.cs.excel.parse.Texts;

@Service
public class TrainingScheduleImporter extends ScheduleImporter {
	public TrainingScheduleImporter() {
		super();
		this.defaultHeaderRowIndex = 2;
	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		SheetImportReport rpt = context.report;
		Term term = context.params.term;
		int classYear = context.params.classYear;

		String defaultDegree = "高职";
		TitleInfo.searchAndValidateTerm(sheet, term);
		TitleInfo titleInfo = TitleInfo.search(sheet, defaultHeaderRowIndex);
		int dataFirstRowIndex = titleInfo.getFollowingDataRowIndex();

		ModelMappingHelper mhelper = context.modelHelper;
		String classYearFilter = Integer.toString(classYear % 2000);

		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row dataRow = sheet.getRow(rowIndex);
			Cell classCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.classColIndex);
			Cell weekRangeCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.weeknoColIndex);
			boolean anyCellStaged = false;

			// parse weekno-range
			byte[] weeknos = TextParser.parseTrainingWeeknoRange(cellString(weekRangeCell));
			// parse class
			String classesName = TextParser.handleMalFormedDegree(cellString(classCell));
			if (classesName.isEmpty() || weeknos == null) {
				String rowStr = Texts.rowString(dataRow);
				if (!rowStr.isEmpty()) {
					String msg = "忽略无效数据行：班级列为空，周数列【" + cellString(weekRangeCell) + "】" + atLocaton(dataRow);
					log.info(rpt.log(msg));
				}
				continue;
			}

			rpt.rowsTotal++;
			log.debug("# 解析班级：【" + classesName + "】" + atLocaton(classCell));
			Class[] pcs = TextParser.parseClasses(classesName, defaultDegree);
			OverlappingChecker overlappingChecker = context.getAttribute(OverlappingChecker.class.getName(),
					OverlappingChecker::new);
			for (Class pc : pcs) {
				String classNameWithDegree = pc.getName() /* + "[" + pc.getDegree() + "]" */;

				if (!classNameWithDegree.contains(classYearFilter)) {
					log.info(rpt.log("忽略班级（非指定年级）：【" + classesName + "】" + atLocaton(dataRow)));
					continue;
				}

//				int count = scheduleRespository.countTrainingByClassAndTermAndWeek(pc.getName(), term.getId(),
//						weeknos);
//				if (count > 0) {
//					log.info(rpt.log("忽略班级（对应周数内已有实训排课记录）：【" + classNameWithDegree + "】" + atLocaton(dataRow)));
//					continue;
//				}

				for (Integer colIndex : titleInfo.timeInfos.keySet()) {
					Cell scheduledCell = dataRow.getCell(colIndex);
					TimeInfo timeInfo = titleInfo.getTimeInfo(scheduledCell);
					String scheduleText;
					ScheduledCourse[] scs;
					// Empty column
					if ((scheduleText = cellString(scheduledCell)).isEmpty())
						continue;

					// parse schedule
					try {
						scs = TextParser.parseTrainingSchedule(scheduleText, weeknos, timeInfo);
					} catch (Exception e) {
						throw new IllegalStateException("实训课表数据格式有误【" + scheduleText + "】" + atLocaton(scheduledCell));
					}

					// deal with parsed schedule
					List<Schedule> schedules = generateParseResult(classNameWithDegree, scs, scheduledCell, timeInfo,
							term, mhelper);
					schedules.forEach(it -> {
						it.setCourseType(Schedule.COURSE_TYPE_TRAINING);
						it.setTrainingType(Schedule.TRAININGTYPE_SCHOOL);
					});
					overlappingChecker.addAll(schedules, scheduledCell);
					mhelper.stageAll(schedules);
					if (!schedules.isEmpty()) {
						anyCellStaged = true;
					}
				} // end of each schedule-cell
			} // end of each class
			if (anyCellStaged)
				rpt.rowsReady++;
		} // end of each row
	}

}
