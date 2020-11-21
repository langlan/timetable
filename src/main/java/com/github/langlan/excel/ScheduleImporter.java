package com.github.langlan.excel;

import static com.github.langlan.excel.TextParser.cellString;
import static com.github.langlan.excel.TextParser.handleMalFormedDegree;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.langlan.dao.ClassCourseRepository;
import com.github.langlan.dao.ScheduleRepository;
import com.github.langlan.domain.ClassCourse;
import com.github.langlan.domain.Schedule;
import com.github.langlan.domain.Term;
import com.github.langlan.excel.TextParser.ScheduledCourse;
import com.github.langlan.excel.TextParser.TimeRange;

@Service
public class ScheduleImporter {
	private static final Log log = LogFactory.getLog(ScheduleImporter.class);
	private @Autowired ClassCourseRepository classCourseRepository;
	private @Autowired ScheduleRepository scheduleRespository;

	@Transactional
	public void importFile(Term term, File file) throws EncryptedDocumentException, IOException {
		try (Workbook wb = WorkbookFactory.create(file, null, true)) {
			doImport(term, wb);
		}
	}

	protected void doImport(Term term, Workbook wb) {
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			doImport(term, sheet);
		}
	}

	class TimeInfo {
		byte dayOfWeek, timeStart, timeEnd;
	}

	protected void doImport(Term term, Sheet sheet) {
		int headerRowIndex = 1, dataFirstRowIndex = 2;
		Pattern timePattern = Pattern.compile("([一二三四五六])/(\\d+)-(\\d+)");
		String weekWords = "一二三四五六";

		Row headerRow = sheet.getRow(headerRowIndex);
		int classColIndex = -1, success = 0;
		if (headerRow == null) {
			log.warn("ignore sheet【" + sheet.getSheetName() + "】");
			return;
		}

		TimeInfo[] timeInfos = new TimeInfo[headerRow.getLastCellNum()];
		for (int i = 0; i < headerRow.getLastCellNum(); i++) {
			Cell cell = headerRow.getCell(i);
			String header = TextParser.cellString(cell);

			if ("课表信息".equals(header)) {
				classColIndex = i;
			} else if (header.indexOf("/") > 0) {
				Matcher m = timePattern.matcher(header);
				if (!m.find()) // TODO: use MessageSource.
					throw new IllegalStateException("未识别的表头【" + header + "】，@Sheet【" + sheet.getSheetName() + "】, 行：【"
							+ headerRow.getRowNum() + 1 + "】");
				timeInfos[i] = new TimeInfo();
				timeInfos[i].dayOfWeek = (byte) (weekWords.indexOf(m.group(1)) + 1);
				timeInfos[i].timeStart = Byte.parseByte(m.group(2));
				timeInfos[i].timeEnd = Byte.parseByte(m.group(3));
			}
		}

		List<Object[]> all = classCourseRepository.findAllWithKeyByTerm(term.getTermYear(), term.getTermMonth());
		Map<String, ClassCourse> indexed = all.parallelStream()
				.collect(Collectors.toMap(it -> it[0].toString(), it -> (ClassCourse) it[1]));
		if (all.size() != indexed.size())
			throw new IllegalStateException("发现现在表中存在重复数据！");

		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			// parse class
			String classNameWithDegree = handleMalFormedDegree(cellString(row.getCell(classColIndex)));
			if (classNameWithDegree.isEmpty())
				continue;

			for (int colIndex = 0; colIndex < timeInfos.length; colIndex++) {
				TimeInfo timeInfo = timeInfos[colIndex]; // column Index.
				String scheduleText;
				ScheduledCourse[] scs;
				// Empty column
				if (timeInfo == null || (scheduleText = cellString(row.getCell(colIndex))).isEmpty())
					continue;

				// parse schedule
				try {
					scs = TextParser.parseSchedule(scheduleText);
				} catch (Exception e) {
					throw new IllegalStateException("课表数据格式有误【" + scheduleText + "】，@sheet: " + sheet.getSheetName()
							+ ", 行：【" + headerRow.getRowNum() + 1 + "】", e);
				}

				// deal with parsed schedule
				for (ScheduledCourse sc : scs) {
					String classCourseKey = classNameWithDegree + "-" + sc.course;
					ClassCourse classCourse = indexed.get(classCourseKey);

					// TODO: TEPM ignore non-mapped class-course for class-18 补充数据?
					if (classCourse == null) {
						if (classCourseKey.contains("18"))
							continue;
						throw new IllegalStateException("无法找到【班级选课】记录：" + classCourseKey);
					}
					
					// validate teacher-name.
					if (!classCourse.getTeacher().getName().equals(sc.teacher)) { // validate
						log.warn("正导入的课程教师名与现存数据不匹配：" + "在导【" + sc.teacher + "】 VS 已有【"
								+ classCourse.getTeacher().getName() + "】" + classCourseKey);
					}

					TimeRange times = sc.timeRange;
					byte weeknoStart = times.weeknoStart, weeknoEnd = times.weeknoEnd;
					byte timeStart = times.timeStart, timeEnd = times.timeEnd;
					
					// try create schedule records and save.
					for (byte weekno = weeknoStart; weekno <= weeknoEnd; weekno++) {
						if (times.oddWeekOnly != null && (weekno % 2 == 0 == times.oddWeekOnly)) {
							log.info("仅" + (times.oddWeekOnly ? "单" : "双") + "数周，排除：" + weekno);
							continue; // exclude even when odd-only, or exclude odd when even-only
						}
						Schedule schedule = new Schedule();
						schedule.setClassCourse(classCourse);
						schedule.setWeekno(weekno);
						schedule.setDayOfWeek(timeInfo.dayOfWeek);
						if (timeStart != timeInfo.timeStart || timeEnd != timeInfo.timeEnd) {
							throw new IllegalStateException(
									"课程时间与表头时间不匹配：表头【" + timeStart + "," + timeEnd + "】，当前【" + timeInfo.timeStart + ","
											+ timeInfo.timeEnd + "】行【" + rowIndex + "】列【" + colIndex + "】@Sheet【】");
						}
						// schedule.setDate(date);
						schedule.setTimeStart(timeStart);
						schedule.setTimeEnd(timeEnd);
						schedule.setRoom(sc.room);
						// scheduleRespository.save(schedule);
						// TODO: set date based on term:weekno+dayOfweek; initialize week & date table if necessary.
						// TODO: mapping room? check for non-unique named room in schedule.
					}
				}

			}
			success++;
		}
		log.info("成功导入【" + success + "/" + (sheet.getLastRowNum() - dataFirstRowIndex + 1) + "】行，@【"
				+ sheet.getSheetName() + "】");

	}
}
