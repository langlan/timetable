package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;
import com.jytec.cs.excel.TitleInfo.TimeInfo;
import com.jytec.cs.excel.api.ImportReport.SheetImportReport;
import com.jytec.cs.excel.parse.HeaderRowNotFountException;
import com.jytec.cs.excel.parse.MergingAreas;
import com.jytec.cs.excel.parse.Regex;
import com.jytec.cs.excel.parse.Texts;

@Service
public class TrainingScheduleImporter2 extends TrainingScheduleImporter {
	private static final Log log = LogFactory.getLog(TrainingScheduleImporter2.class);
	static final String PATTERN_SITE = "实操地点[：:]\\s*(\\S+)";

	class RowContext {
		Cell weekRangeCell, classCell;

	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		SheetImportReport rpt = context.report;
		TitleInfo titleInfo = null;
		try {
			titleInfo = TitleInfo.search(sheet, defaultHeaderRowIndex);
		} catch (HeaderRowNotFountException e) {
			log.warn("忽略表格－" + e.getMessage());
			rpt.ignoredByReason(e.getMessage());
			return;
		}

		Term term = context.params.term;
		String classYearFilter = Integer.toString(context.params.classYear % 2000);
		TitleInfo.searchAndValidateTerm(sheet, term);
		int dataFirstRowIndex = titleInfo.getFollowingDataRowIndex();
		String defaultDegree = "高职";

		ModelMappingHelper mhelper = context.modelHelper;

		Site trainingSite = null;
		int previousFirstDataRowIndex = -1;
		dataLoop: for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				break;
			}

			Cell weekRangeCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.weeknoColIndex);
			Cell classCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.classColIndex);

			RowType rowType = RowType.guessRowTypeByWeekNoCell(weekRangeCell);
			switch (rowType) {
			case DATA:
				if (trainingSite == null) {
					if (previousFirstDataRowIndex == -1) { // remember this row, go back later;
						previousFirstDataRowIndex = rowIndex;
					}
					continue dataLoop;
				} else {
					previousFirstDataRowIndex = -1;
				}
				// take two rows. but do it after reading.
				break;
			case SITE:
				if (trainingSite == null) {
					String siteName = Regex.group(1, Pattern.compile(PATTERN_SITE), Texts.cellString(weekRangeCell));
					trainingSite = mhelper.findSite(siteName, weekRangeCell);
					// go back remembered first-data-row along with the training-site.
					rowIndex = previousFirstDataRowIndex - 1; // the loop itself will increase
				} else { // hit again.
					trainingSite = null;
					previousFirstDataRowIndex = -1;
				}
				continue dataLoop;
			case HEADER:
				titleInfo = TitleInfo.create(sheet, rowIndex);
				trainingSite = null;
				previousFirstDataRowIndex = -1;
				rowIndex = rowIndex + (titleInfo.headerRowSpan - 1);
				continue dataLoop;
			default:
				if (row != null) {
					log.info("Ignore row: " + Texts.rowString(row) + atLocaton(row));
				}

				break dataLoop;
			}

			boolean anyCellImported = false;

			// parse weekno-range
			TimeRange weekRange = TextParser.parseTrainingWeeknoRange(cellString(weekRangeCell));
			// parse class
			String classesName = TextParser.handleMalFormedDegree(cellString(classCell));
			if (classesName.isEmpty() || weekRange == null) {
				log.info("忽略无效数据行：班级列为空，周数列【" + cellString(weekRangeCell) + "】" + atLocaton(row));
				rowIndex++;
				continue;
			} else if (!classesName.contains(classYearFilter)) {
				log.info("忽略整行（不包指定年级的班级）：【" + classesName + "】" + atLocaton(row));
				rowIndex++;
				rpt.rowsTotal++;
				continue;
			}

			Class[] pcs = TextParser.parseClasses(classesName, defaultDegree);
			log.debug("# 解析班级：【" + classesName + "】" + atLocaton(row));
			Assert.isTrue(pcs.length > 0, "解析班级失败，疑似格式有误：" + classesName + atLocaton(classCell));
			OverlappingChecker overlappingChecker = context.getAttribute(OverlappingChecker.class.getName(),
					OverlappingChecker::new);
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
					String courseName = cellString(scheduledCell);
					String teacherName = cellString(sheet.getRow(rowIndex + 1).getCell(colIndex));
					if ((courseName).isEmpty())
						continue;
					ScheduledCourse sc = new ScheduledCourse();
					sc.teacher = teacherName;
					sc.course = courseName;
					sc.site = trainingSite.getName();
					sc.timeRange = new TimeRange();
					sc.timeRange.weeknoStart = weekRange.weeknoStart;
					sc.timeRange.weeknoEnd = weekRange.weeknoEnd;
					sc.timeRange.timeStart = timeInfo.timeStart;
					sc.timeRange.timeEnd = timeInfo.timeEnd;

					ScheduledCourse[] scs = new ScheduledCourse[] { sc };
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
						anyCellImported = true;
					}
				} // end of each schedule-cell
			} // end of each class
			rowIndex++; // take two rows.
			rpt.rowsTotal++;
			if (anyCellImported)
				rpt.rowsReady++;
		} // end of each row
		log.info("准备导入【" + rpt.rowsReady + "/" + rpt.rowsTotal + "】行" + atLocaton(sheet));
		// Should it be called through other UI to keep data-import and date-build independent.
	}

	enum RowType {
		HEADER, DATA, SITE, MADE_BY, MAIN_TITLE, UNKNOWN;

		static final Pattern p = Pattern.compile("(周\\s*数)|(\\d+)|(实操地点.+)");

		static RowType guessRowTypeByWeekNoCell(Cell weekRangeCell) {
			String cellString = Texts.cellString(weekRangeCell);
			Matcher m = p.matcher(cellString);
			if (m.find()) {
				if (m.group(1) != null)
					return HEADER;
				else if (m.group(2) != null) {
					return DATA;
				} else {
					Assert.isTrue(m.group(3) != null, "DEV：更新正则须更改代码逻辑");
					return SITE;
				}
			}
			return UNKNOWN;
		}
	}

}
