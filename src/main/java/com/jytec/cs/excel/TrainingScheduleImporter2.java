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

		boolean siteNameResolved = false, siteModelResolved = false;
		String trainingSiteName = null;
		Site trainingSite = null;
		int previousFirstDataRowIndex = -1;
		dataLoop: for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row dataRow = sheet.getRow(rowIndex);
			if (dataRow == null) {
				break;
			}

			Cell weekRangeCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.weeknoColIndex);
			Cell classCell = MergingAreas.getCellWithMerges(sheet, rowIndex, titleInfo.classColIndex);

			RowType rowType = RowType.guessRowTypeByWeekNoCell(weekRangeCell);
			switch (rowType) {
			case DATA:
				if (!siteNameResolved) {
					if (previousFirstDataRowIndex == -1) { // remember this row, go back later;
						previousFirstDataRowIndex = rowIndex;
					}
					rowIndex = rowIndex + MergingAreas.getCellRowSpan(weekRangeCell) - 1;
					continue dataLoop;
				} else {
					previousFirstDataRowIndex = -1;
				}
				// take two rows. but do it after reading.
				break;
			case SITE:
				if (!siteNameResolved) {
					trainingSiteName = Regex.group(1, Pattern.compile(PATTERN_SITE), Texts.cellString(weekRangeCell));
					siteNameResolved = true;
					// trainingSite = mhelper.findSite(siteName, weekRangeCell);
					// go back remembered first-data-row along with the training-site.
					rowIndex = previousFirstDataRowIndex - 1; // the loop itself will increase
				} else { // hit again.
					siteNameResolved = false;
					// trainingSite = null;
					previousFirstDataRowIndex = -1;
				}
				continue dataLoop;
			case HEADER:
				titleInfo = TitleInfo.create(sheet, rowIndex);
				siteNameResolved = false;
				previousFirstDataRowIndex = -1;
				rowIndex = rowIndex + (titleInfo.headerRowSpan - 1);
				continue dataLoop;
			default:
				if (dataRow != null) {
					log.info("Ignore row: " + Texts.rowString(dataRow) + atLocaton(dataRow));
				}

				break dataLoop;
			}
			/////////////// determine cell row span //////////////
			DataRowSpan drs = new DataRowSpan(dataRow, titleInfo);
			boolean anyCellImported = false;
			// parse weekno-range
			byte[] weeknos= TextParser.parseTrainingWeeknoRange(cellString(weekRangeCell));
			// parse class
			String classesName = TextParser.handleMalFormedDegree(cellString(classCell));
			if (classesName.isEmpty() || weeknos == null) {
				throw new IllegalStateException("请勿在表头与实操地点之间保留空行！");
			} else if (!classesName.contains(classYearFilter)) {
				log.info(rpt.log("忽略整行（不包含指定年级的班级）：【" + classesName + "】" + atLocaton(dataRow, false)));
				rowIndex += drs.getDataRowSpan() - 1;
				rpt.rowsTotal++;
				continue;
			}

			// TODO: some site name not machine-readable: site-name1，周三（1-2）site-name2
			// @ schedule-training-21.xls[电技18（修改）]
			if (!siteModelResolved) {
				Assert.isTrue(siteNameResolved, "DEV: Check Code");
				trainingSite = mhelper.findSite(trainingSiteName, weekRangeCell);
				siteModelResolved = true;
			}

			// TODO: some sheet takes 3 rows: course-name takes 1, but teacher-name-takes 2. @
			Class[] pcs = TextParser.parseClasses(classesName, defaultDegree);
			log.debug("# 解析班级：【" + classesName + "】" + atLocaton(dataRow));
			Assert.isTrue(pcs.length > 0, "解析班级失败，疑似格式有误：" + classesName + atLocaton(classCell));
			OverlappingChecker overlappingChecker = context.getAttribute(OverlappingChecker.class.getName(),
					OverlappingChecker::new);
			for (Class pc : pcs) {
				String classNameWithDegree = pc.getName() /* + "[" + pc.getDegree() + "]" */;

				if (!classNameWithDegree.contains(classYearFilter)) {
					log.info(rpt.log("忽略班级（非指定年级）：【" + classesName + "】" + atLocaton(dataRow)));
					continue;
				}

				for (Integer colIndex : titleInfo.timeInfos.keySet()) {
					Cell scheduledCell = dataRow.getCell(colIndex);
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
					sc.timeRange.weeknos = weeknos;
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
			rowIndex += drs.getDataRowSpan() - 1; // take two rows or more.
			rpt.rowsTotal++;
			if (anyCellImported)
				rpt.rowsReady++;
		} // end of each row
		log.info("准备导入【" + rpt.rowsReady + "/" + rpt.rowsTotal + "】行" + atLocaton(sheet));
		// Should it be called through other UI to keep data-import and date-build independent.
	}

	static enum RowType {
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

	static class DataRowSpan {
		int courseNameCellRowSpan, teacherNameCellRowSpan;

		public DataRowSpan(Row dataRow, TitleInfo titleInfo) {
			for (Integer colIndex : titleInfo.timeInfos.keySet()) {
				Cell courseNameCell = dataRow.getCell(colIndex);
				if (courseNameCell != null) {
					int _courseNameCellRowSpan = MergingAreas.getCellRowSpan(courseNameCell);
					if (courseNameCellRowSpan > 0) {
						Assert.isTrue(courseNameCellRowSpan == _courseNameCellRowSpan,
								"所有课程名单元格的不必合并，若合并则合并行数须保持一致！" + atLocaton(courseNameCell));
					} else {
						courseNameCellRowSpan = _courseNameCellRowSpan;
					}
					Cell teacherNameCell = dataRow.getSheet().getRow(dataRow.getRowNum() + courseNameCellRowSpan)
							.getCell(colIndex);
					if (teacherNameCell != null) {
						int _teacherNameCellRowSpan = MergingAreas.getCellRowSpan(teacherNameCell);
						if (teacherNameCellRowSpan > 0) {
							Assert.isTrue(_teacherNameCellRowSpan == teacherNameCellRowSpan,
									"所有教师名单元格不必全并，若合并则合并行数须保持一致！" + atLocaton(courseNameCell));
						} else {
							teacherNameCellRowSpan = _teacherNameCellRowSpan;
						}
					}
				}
			}
			Assert.isTrue(courseNameCellRowSpan > 0, "数据行合并行数检测失败！");
			Assert.isTrue(teacherNameCellRowSpan > 0, "数据行合并行数检测失败！");
		}

		public int getDataRowSpan() {
			return courseNameCellRowSpan + teacherNameCellRowSpan;
		}

	}

}
