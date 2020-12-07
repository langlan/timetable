package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import com.jytec.cs.excel.TrainingScheduleImporter.TitleInfo.TimeInfo;
import com.jytec.cs.excel.parse.MergingAreas;
import com.jytec.cs.excel.parse.Regex;

@Service
public class TrainingScheduleImporter {
	private static final Log log = LogFactory.getLog(TrainingScheduleImporter.class);
	// private @Autowired ClassRepository classRepository;
	private @Autowired ScheduleRepository scheduleRespository;
	private @Autowired ApplicationContext applicationContext;

	@Transactional
	public void importFile(Term term, int classYear, File file) throws EncryptedDocumentException, IOException {
		try (Workbook wb = WorkbookFactory.create(file, null, true)) {
			doImport(term, classYear, wb);
		}
	}

	protected void doImport(Term term, int classYear, Workbook wb) {
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			doImport(term, classYear, sheet);
		}
	}

	// TODO: Preview for importing.
	protected void doImport(Term term, int classYear, Sheet sheet) {
		String defaultDegree = "高职";
		int headerRowIndex = 2, dataFirstRowIndex = headerRowIndex + 1;
		String weeknoColHeaderPattern = "周\\s*数";
		String classNameColHeaderPattern = "班\\s*级";
		String dayOfWeekHeaderPattern = "星期([一二三四五六])";
		String dayOfWeekWords = "一二三四五六"; // for converting to integer.
		Pattern timeRangeSubHeaderPattern = Pattern.compile("(\\d+)[^\\d]+(\\d+)");
		String otherAcceptableHeaderPattern = "系\\s*部|备\\s*注";

		Row headerRow = sheet.getRow(headerRowIndex);
		if (headerRow == null) {
			log.warn("ignore sheet【" + sheet.getSheetName() + "】");
			return;
		}
		// handle title-rows(3 or 2)
		TitleInfo titleInfo = new TitleInfo();
		int titleRowSpan = -1;
		for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
			Cell headerCell = headerRow.getCell(colIndex);
			String header = cellString(headerCell);
			if (header.isEmpty())
				continue;

			if (Pattern.matches(weeknoColHeaderPattern, header)) {
				titleInfo.weeknoColIndex = headerCell.getColumnIndex();
				CellRangeAddress ma = MergingAreas.getMergingArea(headerCell);
				titleRowSpan = (ma.getLastRow() - ma.getFirstRow() + 1); // row-span
				dataFirstRowIndex = headerRowIndex + titleRowSpan;
			} else if (Pattern.matches(classNameColHeaderPattern, header)) {
				titleInfo.classColIndex = headerCell.getColumnIndex();
			} else if (Pattern.matches(dayOfWeekHeaderPattern, header)) {
				String weekWord = Regex.group(1, Pattern.compile(dayOfWeekHeaderPattern), header);
				int dayOfWeek = dayOfWeekWords.indexOf(weekWord) + 1;

				CellRangeAddress ma = MergingAreas.getMergingArea(headerCell);
				for (int colIndex4TR = ma.getFirstColumn(); colIndex4TR <= ma.getLastColumn(); colIndex4TR++) {
					for (int rowIndex = headerRowIndex + 1; rowIndex < headerRowIndex + titleRowSpan; rowIndex++) {
						Cell timeRangeCell = MergingAreas.getCell(sheet, rowIndex, colIndex4TR);
						String subHeader = cellString(timeRangeCell);
						if (!Regex.matchesPart(timeRangeSubHeaderPattern, subHeader))
							continue; // maybe a.m. | p.m.

						String[] timeRange = Regex.groups(timeRangeSubHeaderPattern, subHeader);
						TitleInfo.TimeInfo timeInfo = new TitleInfo.TimeInfo();
						timeInfo.dayOfWeek = (byte) dayOfWeek;
						timeInfo.timeStart = Byte.parseByte(timeRange[1]);
						timeInfo.timeEnd = Byte.parseByte(timeRange[2]);
						titleInfo.timeInfos.put(colIndex4TR, timeInfo);
						Assert.isTrue(colIndex4TR == timeRangeCell.getColumnIndex(), "课时列 index 计算错误！");
						log.debug("标记课时列【" + timeRangeCell + "】" + atLocaton(timeRangeCell));
					}
				}
			} else if (!Pattern.matches(otherAcceptableHeaderPattern, header)) {
				throw new IllegalStateException("未识别的表头【" + header + "】" + atLocaton(headerCell));
			}
		}

		ModelMappingHelper mhelper = applicationContext.getBean(ModelMappingHelper.class).term(term);
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
			for (Class pc : pcs) {
				String classNameWithDegree = pc.getName() /* + "[" + pc.getDegree() + "]" */;

				if (!classNameWithDegree.contains(classYearFilter)) {
					log.info("忽略班级（非指定年级）：【" + classesName + "】" + atLocaton(row));
					continue;
				}

				// fixed issue : same class may have multiple rows to correspond different week-no.
				// so we can not use term+class (use term+class+week-no instead) to determine duplicate import.
				int count = scheduleRespository.countTrainingByClassAndTermAndWeek(pc.getName(), pc.getDegree(),
						term.getId(), weekRange.weeknoStart, weekRange.weeknoEnd);
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
					for (ScheduledCourse sc : scs) {
						TimeRange times = sc.timeRange;
						byte weeknoStart = times.weeknoStart, weeknoEnd = times.weeknoEnd;
						byte timeStart = times.timeStart, timeEnd = times.timeEnd;
						ClassCourse classCourse = mhelper.findClassCourse(classNameWithDegree, sc.course,
								scheduledCell);
						Teacher teacher = mhelper.findTeacher(sc.teacher, classCourse, scheduledCell);
						Site site = mhelper.findSite(sc.site, scheduledCell);

						// try create schedule records and save.
						for (byte weekno = weeknoStart; weekno <= weeknoEnd; weekno++) {
							if (times.oddWeekOnly != null && ((weekno % 2 == 0) == times.oddWeekOnly)) {
								log.debug("仅" + (times.oddWeekOnly ? "单" : "双") + "数周，排除：" + weekno
										+ atLocaton(scheduledCell));
								continue; // exclude even when odd-only, or exclude odd when even-only
							}
							Schedule schedule = new Schedule();
							// TODO: for TRAININGTYPE_ENTERPRISE
							schedule.setTrainingType(Schedule.TRAININGTYPE_SCHOOL);
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
							scheduleRespository.save(schedule);
							rowImported = true;
						}
					} // end of each course
				} // end of each schedule-cell
			} // end of each class
			if (rowImported)
				imported++;
		} // end of each row
		log.info("成功导入【" + imported + "/" + totalRow + "】行，@【" + sheet.getSheetName() + "】");
		// Should it be called through other UI to keep data-import and date-build independent.
		scheduleRespository.updateDateByTerm(term.getId());
	}

	static class TitleInfo {
		Map<Integer, TimeInfo> timeInfos = new HashMap<>();
		// just for debug and help timeInfo when weekno, time-range in different rows.
		// Map<Integer, Integer> weeknos = new HashMap<>();
		// for data row
		int classColIndex = -1, weeknoColIndex = -1;

		/** return the time-info {dayOfWeek, timeStart, timeEnd} for the corresponding cell */
		TimeInfo getTimeInfo(Cell cell) {
			TimeInfo timeInfo = timeInfos.get(cell.getColumnIndex());
			CellRangeAddress mergedArea = MergingAreas.getMergingArea(cell);
			if (mergedArea != null) {
				Assert.isTrue(mergedArea.getFirstColumn() == cell.getColumnIndex(), "Merging algorithm problems.");
				TimeInfo end = timeInfos.get(mergedArea.getLastColumn());
				Assert.isTrue(timeInfo.dayOfWeek == end.dayOfWeek, "TODO: 处理跨天的单元格合并！");
				TimeInfo ret = new TimeInfo();
				ret.dayOfWeek = timeInfo.dayOfWeek;
				ret.timeStart = timeInfo.timeStart;
				ret.timeEnd = end.timeEnd;
				timeInfo = ret;
			}
			return timeInfo;
		}

		/** dayOfWeek, timeStart, timeEnd */
		static class TimeInfo {
			byte dayOfWeek, timeStart, timeEnd;
		}
	}

}
