package com.jytec.cs.excel;

import static com.jytec.cs.excel.TextParser.atLocaton;
import static com.jytec.cs.excel.TextParser.cellString;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.NonUniqueResultException;
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
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
import com.jytec.cs.service.AutoCreateService;

@Service
public class ScheduleImporter {
	private static final Log log = LogFactory.getLog(ScheduleImporter.class);
	private @Autowired ClassCourseRepository classCourseRepository;
	// private @Autowired ClassRepository classRepository;
	private @Autowired TeacherRepository teacherRepository;
	private @Autowired SiteRepository siteRepository;
	private @Autowired ScheduleRepository scheduleRespository;
	private @Autowired AutoCreateService autoCreateService;

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

	/** Time information from header */
	class TimeInfo {
		byte dayOfWeek, timeStart, timeEnd;
	}

	// TODO: Preview for importing.
	protected void doImport(Term term, int classYear, Sheet sheet) {
		int headerRowIndex = 1, dataFirstRowIndex = headerRowIndex + 1;
		Pattern timePattern = Pattern.compile("([一二三四五六])/(\\d+)-(\\d+)");
		String weekWords = "一二三四五六";

		Row headerRow = sheet.getRow(headerRowIndex);
		if (headerRow == null) {
			log.warn("ignore sheet【" + sheet.getSheetName() + "】");
			return;
		}

		// try recognize term and validate.
		Row titleRow = sheet.getRow(0);
		String title = TextParser.rowString(titleRow);
		Term pTerm = TextParser.parseTerm(title);
		if (pTerm == null) {
			log.warn("无法从标题中识别学期：" + title + atLocaton(titleRow));
		} else {
			if (pTerm.getTermYear() != term.getTermYear() || pTerm.getTermMonth() != term.getTermMonth())
				throw  new IllegalArgumentException("表格标题【" + title + "】与指定的学期不同！");
		}
		
		// handle columns header 
		int classColIndex = -1;
		TimeInfo[] timeInfos = new TimeInfo[headerRow.getLastCellNum()];
		for (int i = 0; i < headerRow.getLastCellNum(); i++) {
			Cell headerCell = headerRow.getCell(i);
			String header = TextParser.cellString(headerCell);

			if ("课表信息".equals(header)) {
				classColIndex = i;
			} else if (header.indexOf("/") > 0) {
				Matcher m = timePattern.matcher(header);
				if (!m.find()) // TODO: use MessageSource?
					throw new IllegalStateException("未识别的表头【" + header + "】" + TextParser.atLocaton(headerCell));
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
		int imported = 0, totalRow = 0;
		for (int rowIndex = dataFirstRowIndex; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			Cell cell = row.getCell(classColIndex);
			totalRow++;
			// parse class
			String classNameWithDegree = TextParser.handleMalFormedDegree(cellString(cell));
			if (classNameWithDegree.isEmpty())
				continue;
			if (!classNameWithDegree.contains(classYearFilter)) {
				log.info("忽略班级：【" + classNameWithDegree + "】" + atLocaton(row));
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
			if (scheduleRespository.countNonTrainingByClassAndTerm(pc.getName(), pc.getDegree(), term.getTermYear(),
					term.getTermMonth()) > 0) {
				log.info("忽略班级（已有非实训排课记录）：【" + classNameWithDegree + "】" + atLocaton(row));
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
					throw new IllegalStateException("课表数据格式有误【" + scheduleText + "】" + atLocaton(scheduledCell));
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
							String msg = "课程教师名与选课数据不匹配：" + "在导【" + sc.teacher + "】 VS 选课【"
									+ classCourse.getTeacherNames() + "】@" + classCourseKey;
							log.warn(msg);
						}
						teacher = teacherRepository.findByName(sc.teacher).orElseGet(() -> {
							String msg = "找不到教师【" + sc.teacher + "】【" + scheduleText + "】" + atLocaton(scheduledCell);
							// throw new IllegalStateException(msg);
							log.warn(msg);
							return autoCreateService.createTeacherWithAutoCode(sc.teacher);
						});
					}

					// locate site
					// NODE: name is not actually a unique key. but for theory course, we suppose so.
					try {
						site = siteRepository.findUniqueByName(sc.site).orElseGet(() -> {
							String msg = "找不到上课地点【" + sc.site + "】【" + scheduleText + "】" + atLocaton(scheduledCell);
							log.warn(msg);
							// throw new IllegalStateException(warn);
							return autoCreateService.createSiteWithAutoCode(sc.site);
						});
					} catch (IncorrectResultSizeDataAccessException | NonUniqueResultException e) {
						throw new IllegalStateException("非唯一：存在多个同名上课地点【" + sc.site + "】" + atLocaton(scheduledCell));
					}

					if (timeStart != timeInfo.timeStart || timeEnd != timeInfo.timeEnd) {
						throw new IllegalStateException("课程时间与表头时间不匹配：表头【" + timeStart + "," + timeEnd + "】，当前【"
								+ timeInfo.timeStart + "," + timeInfo.timeEnd + "】" + atLocaton(scheduledCell));
					}

					// try create schedule records and save.
					for (byte weekno = weeknoStart; weekno <= weeknoEnd; weekno++) {
						if (times.oddWeekOnly != null && ((weekno % 2 == 0) == times.oddWeekOnly)) {
							log.debug("仅" + (times.oddWeekOnly ? "单" : "双") + "数周，排除：" + weekno
									+ atLocaton(scheduledCell));
							continue; // exclude even when odd-only, or exclude odd when even-only
						}
						Schedule schedule = new Schedule();
						schedule.setTrainingType(Schedule.TRAININGTYPE_NON);
						schedule.setTermYear(term.getTermYear());
						schedule.setTermMonth(term.getTermMonth());
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
						// TODO: set date based on term:weekno+dayOfweek; initialize week & date table if necessary.
					}
				}

			}
		}
		log.info("成功导入【" + imported + "/" + totalRow + "】行，@【" + sheet.getSheetName() + "】");
		// Should it be called through other UI to keep data-import and date-build independent.
		scheduleRespository.updateDateByTerm(term.getTermYear(), term.getTermMonth());
	}

}
