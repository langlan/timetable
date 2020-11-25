package com.jytec.cs.excel;

import static com.jytec.cs.excel.TextParser.cellString;

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

import com.jytec.cs.dao.ClassCourseRepository;
import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.dao.TeacherRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.TextParser.TimeRange;

@Service
public class ScheduleImporter {
	private static final Log log = LogFactory.getLog(ScheduleImporter.class);
	private @Autowired ClassCourseRepository classCourseRepository;
	// private @Autowired ClassRepository classRepository;
	private @Autowired TeacherRepository teacherRepository;
	private @Autowired SiteRepository siteRepository;
	private @Autowired ScheduleRepository scheduleRespository;

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

	class TimeInfo {
		byte dayOfWeek, timeStart, timeEnd;
	}

	protected void doImport(Term term, int classYear, Sheet sheet) {
		int headerRowIndex = 1, dataFirstRowIndex = 2;
		Pattern timePattern = Pattern.compile("([一二三四五六])/(\\d+)-(\\d+)");
		String weekWords = "一二三四五六";

		Row headerRow = sheet.getRow(headerRowIndex);
		if (headerRow == null) {
			log.warn("ignore sheet【" + sheet.getSheetName() + "】");
			return;
		}

		// handle title-row
		int classColIndex = -1;
		TimeInfo[] timeInfos = new TimeInfo[headerRow.getLastCellNum()];
		for (int i = 0; i < headerRow.getLastCellNum(); i++) {
			Cell cell = headerRow.getCell(i);
			String header = TextParser.cellString(cell);

			if ("课表信息".equals(header)) {
				classColIndex = i;
			} else if (header.indexOf("/") > 0) {
				Matcher m = timePattern.matcher(header);
				if (!m.find()) // TODO: use MessageSource?
					throw new IllegalStateException("未识别的表头【" + header + "】，@Sheet【" + sheet.getSheetName() + "】, 行：【"
							+ headerRow.getRowNum() + 1 + "】");
				timeInfos[i] = new TimeInfo();
				timeInfos[i].dayOfWeek = (byte) (weekWords.indexOf(m.group(1)) + 1);
				timeInfos[i].timeStart = Byte.parseByte(m.group(2));
				timeInfos[i].timeEnd = Byte.parseByte(m.group(3));
			}
		}

		List<Object[]> all = classCourseRepository.findAllWithKeyByTerm(term.getTermYear(), term.getTermMonth());
		Map<String, ClassCourse> indexed = all.stream()
				.collect(Collectors.toMap(it -> it[0].toString(), it -> (ClassCourse) it[1]));
		if (all.size() != indexed.size())
			throw new IllegalStateException("发现现存【班级选课表】中存在重复数据！");

		String classYearFilter = Integer.toString(classYear % 2000);
		int imported = 0, total = 0;
		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			Cell cell = row.getCell(classColIndex);
			total++;
			// parse class
			String classNameWithDegree = TextParser.handleMalFormedDegree(cellString(cell));
			if (classNameWithDegree.isEmpty())
				continue;
			if (!classNameWithDegree.contains(classYearFilter)) {
				log.debug("忽略班级：" + classNameWithDegree);
				continue;
			}

			Class pc = TextParser.parseClass(classNameWithDegree);
			// Class theClass = classRepository.findByNameAndDegree(pc.getName(), pc.getDegree());
			// 因每行为一个班级课表，故幂等/防重导策略以班级为单位：
			// 1. 当发现有该班级的课程安排时，忽略。【暂时采用】
			// 2. 删除已有班级的课程安排数据，而后正常导入
			// 2.1 优点：支持/适合【修改导入文件后重新导入的情景】
			// 2.2 缺点：在数据未曾修改时，浪费 id
			// 3. 全部清空，全量导入
			// 3.1 优点：适合【测试场景】、【全量重导需求（排课功能脱离或表表隔离）】
			if (scheduleRespository.countByClassAndTerm(pc.getName(), pc.getDegree(), term.getTermYear(),
					term.getTermMonth()) > 0) {
				log.info("发现已存在班级的排课信息，忽略该班级 @【" + classNameWithDegree + "】");
				continue;
			}

			imported++;
			for (int colIndex = 0; colIndex < timeInfos.length; colIndex++) {
				TimeInfo timeInfo = timeInfos[colIndex]; // column Index.
				String scheduleText;
				ScheduledCourse[] scs;
				// Empty column
				Cell scheduledCell = row.getCell(colIndex);
				if (timeInfo == null || (scheduleText = cellString(scheduledCell)).isEmpty())
					continue;

				// parse schedule
				try {
					scs = TextParser.parseSchedule(scheduleText);
				} catch (Exception e) {
					throw new IllegalStateException(
							"课表数据格式有误【" + scheduleText + "】，@sheet: " + sheet.getSheetName() + cell.getAddress(), e);
				}

				// deal with parsed schedule
				for (ScheduledCourse sc : scs) {
					TimeRange times = sc.timeRange;
					byte weeknoStart = times.weeknoStart, weeknoEnd = times.weeknoEnd;
					byte timeStart = times.timeStart, timeEnd = times.timeEnd;
					String classCourseKey = classNameWithDegree + "-" + sc.course;
					ClassCourse classCourse = indexed.get(classCourseKey);
					Teacher teacher = null;
					Site site = null;

					if (classCourse == null) {
						throw new IllegalStateException("无法找到【班级选课】记录：" + classCourseKey);
					}

					// validate/locate-by teacher-name.
					if (classCourse.getTeacher().getName().equals(sc.teacher)) { // validate
						teacher = classCourse.getTeacher();
					} else {
						if (!classCourse.getTeacherNames().contains(sc.teacher)) {
							log.warn("正导入的课程教师名与现存数据不匹配：" + "在导【" + sc.teacher + "】 VS 已有【"
									+ classCourse.getTeacherNames() + "】" + classCourseKey);
						}
						teacher = teacherRepository.findByName(sc.teacher).orElseGet(() -> {
							throw new IllegalStateException("找不到教师【" + sc.teacher + "】【" + scheduleText + "】@sheet:"
									+ sheet.getSheetName() + ": " + scheduledCell.getAddress());
						});
					}

					// locate site
					// TODO. name is not actually a key. but for theory class, it maybe...
					site = siteRepository.findUniqueByName(sc.site).orElseGet(() -> {
						String warn = "找不到上课地点【" + sc.site + "】【" + scheduleText + "】@sheet:" + sheet.getSheetName()
								+ ": " + scheduledCell.getAddress();
						// log.warn);
						return null;
						// throw new IllegalStateException(warn);
					});

					// try create schedule records and save.
					for (byte weekno = weeknoStart; weekno <= weeknoEnd; weekno++) {
						if (times.oddWeekOnly != null && (weekno % 2 == 0 == times.oddWeekOnly)) {
							log.debug("仅" + (times.oddWeekOnly ? "单" : "双") + "数周，排除：" + weekno);
							continue; // exclude even when odd-only, or exclude odd when even-only
						}
						Schedule schedule = new Schedule();
						schedule.setTermYear(term.getTermYear());
						schedule.setTermMonth(term.getTermMonth());
						schedule.setTheClass(classCourse.getTheClass());
						schedule.setCourse(classCourse.getCourse());
						// schedule.setClassCourse(classCourse);
						schedule.setTeacher(teacher);
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
						schedule.setSite(site);
						scheduleRespository.save(schedule);
						// TODO: set date based on term:weekno+dayOfweek; initialize week & date table if necessary.
					}
				}

			}
		}
		log.info("成功导入【" + imported + "/" + total + "】行，@【" + sheet.getSheetName() + "】");

	}
}
