package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.TextParser.ScheduledCourse;
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
			String classesNames = TextParser.handleMalFormedDegree(cellString(classCell));
			if (classesNames.isEmpty() || weeknos == null) {
				String rowStr = Texts.rowString(dataRow);
				if (!rowStr.isEmpty()) {
					String msg = "忽略无效数据行：班级列为空，周数列【" + cellString(weekRangeCell) + "】" + atLocaton(dataRow);
					log.info(rpt.log(msg));
				}
				continue;
			}
			if (!classesNames.contains(classYearFilter)) {
				log.info(rpt.log("忽略班级（非指定年级）：【" + classesNames + "】" + atLocaton(dataRow)));
				continue; // 不同年级的班级合班上课不太可能，故不再单独检测
			}

			rpt.rowsTotal++;
			log.debug("# 解析班级：【" + classesNames + "】" + atLocaton(classCell));
			Class[] pcs = TextParser.parseClasses(classesNames, defaultDegree);
			List<String> cnl = Arrays.asList(pcs).stream().map(Class::getName).collect(Collectors.toList());
			String[] classNames = cnl.toArray(new String[cnl.size()]);
			for (Integer colIndex : titleInfo.timeInfos.keySet()) {
				Cell scheduledCell = dataRow.getCell(colIndex);
				TimeInfo timeInfo = titleInfo.getTimeInfo(scheduledCell);
				String scheduleText = cellString(scheduledCell);
				// Empty column
				if (scheduleText.isEmpty())
					continue;

				// parse schedule
				ScheduledCourse[] scs = TextParser.parseTrainingSchedule(scheduleText, weeknos, timeInfo);
				mhelper.resolve(scs, classNames, timeInfo.dayOfWeek, scheduledCell);

				// deal with parsed schedule
				List<Schedule> schedules = generate(scs, scheduledCell, timeInfo, term, mhelper);
				schedules.forEach(it -> {
					it.setCourseType(Schedule.COURSE_TYPE_TRAINING);
					it.setTrainingType(Schedule.TRAININGTYPE_SCHOOL);
					String siteName = it.getSite().getName();
					if(siteName!=null && siteName.contains("企业")) {
						it.setTrainingType(Schedule.TRAININGTYPE_ENTERPRISE);
					}
				});

				mhelper.stageAll(schedules);
				anyCellStaged |= !schedules.isEmpty();
			} // end of each schedule-cell
			if (anyCellStaged)
				rpt.rowsReady++;
		} // end of each row
	}

	@Override
	protected boolean acceptScheduleCell(ScheduledCourse sc, Cell scheduledCell, ModelMappingHelper mhelper) {
		for (ClassCourse cc : sc.classCourses) {
			int count = mhelper.countTrainingScheduleByWeek(cc, sc.timeRange.weeknos);
			if (count > 0) {
				log.info("忽略班级选课（对应周数内已有实训排课记录）：【" + cc.getClass().getName() + "-" + sc.courseName + "】"
						+ atLocaton(scheduledCell));
				return false;
			}
		}

		return true;
	}

}
