package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;
import com.jytec.cs.excel.TitleInfo.TimeInfo;
import com.jytec.cs.excel.parse.MergingAreas;

@Service
public class TrainingScheduleImporter extends ScheduleImporter {
	private static final Log log = LogFactory.getLog(TrainingScheduleImporter.class);

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		Term term = context.params.term;
		int classYear = context.params.classYear;
		Assert.notNull(term, "参数学期不可为空！");
		Assert.notNull(classYear, "参数年级不可为空！");

		String defaultDegree = "高职";
		int headerRowIndex = 2, dataFirstRowIndex = headerRowIndex + 1;
		TitleInfo.searchAndValidateTerm(sheet, term);
		TitleInfo titleInfo = TitleInfo.search(sheet, headerRowIndex);

		ModelMappingHelper mhelper = context.modelHelper;
		String classYearFilter = Integer.toString(classYear % 2000);
		int imported = 0, totalRow = 0;
		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			Cell classCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.classColIndex);
			Cell weekRangeCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.weeknoColIndex);
			boolean rowImported = false;

			// parse weekno-range
			TimeRange weekRange = TextParser.parseTrainingWeeknoRange(cellString(weekRangeCell));
			// parse class
			String classesName = TextParser.handleMalFormedDegree(cellString(classCell));
			if (classesName.isEmpty() || weekRange == null) {
				log.info("忽略无效数据行：班级列为空，周数列【" + cellString(weekRangeCell) + "】" + atLocaton(row));
				continue;
			}

			totalRow++;
			log.debug("# 解析班级：【" + classesName + "】" + atLocaton(row));
			Class[] pcs = TextParser.parseClasses(classesName, defaultDegree);
			OverlappingChecker overlappingChecker = context.getAttribute(OverlappingChecker.class.getName(), OverlappingChecker::new);
			for (Class pc : pcs) {
				String classNameWithDegree = pc.getName() /* + "[" + pc.getDegree() + "]" */;

				if (!classNameWithDegree.contains(classYearFilter)) {
					log.info("忽略班级（非指定年级）：【" + classesName + "】" + atLocaton(row));
					continue;
				}

				// fixed issue : same class may have multiple rows to correspond different week-no.
				// so we can not use term+class (use term+class+week-no instead) to determine duplicate import.
				int count = scheduleRespository.countTrainingByClassAndTermAndWeek(pc.getName(), term.getId(),
						weekRange.weeknoStart, weekRange.weeknoEnd);
				if (count > 0) {
					log.info("忽略班级（对应周数内已有实训排课记录）：【" + classNameWithDegree + "】" + atLocaton(row));
					continue;
				}

				for (Integer colIndex : titleInfo.timeInfos.keySet()) {
					Cell scheduledCell = row.getCell(colIndex);
					TimeInfo timeInfo = titleInfo.getTimeInfo(scheduledCell);
					String scheduleText;
					ScheduledCourse[] scs;
					// Empty column
					if ((scheduleText = cellString(scheduledCell)).isEmpty())
						continue;

					// parse schedule
					try {
						scs = TextParser.parseTrainingSchedule(scheduleText, weekRange, timeInfo);
					} catch (Exception e) {
						throw new IllegalStateException("实训课表数据格式有误【" + scheduleText + "】" + atLocaton(scheduledCell));
					}

					// deal with parsed schedule
					List<Schedule> schedules = generateParseResult(classNameWithDegree, scs, scheduledCell, timeInfo, term, mhelper);
					schedules.forEach(it->{
						it.setCourseType(Schedule.COURSE_TYPE_TRAINING);
						it.setTrainingType(Schedule.TRAININGTYPE_SCHOOL);
					});
					overlappingChecker.addAll(schedules, scheduledCell);
					mhelper.stageAll(schedules);
					rowImported = true;
				} // end of each schedule-cell
			} // end of each class
			if (rowImported)
				imported++;
		} // end of each row
		log.info("成功导入【" + imported + "/" + totalRow + "】行，@【" + sheet.getSheetName() + "】");
		// Should it be called through other UI to keep data-import and date-build independent.
		scheduleRespository.updateDateByTerm(term.getId());
	}
}
