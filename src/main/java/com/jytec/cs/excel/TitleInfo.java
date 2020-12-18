package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static com.jytec.cs.excel.parse.Texts.cellString;
import static com.jytec.cs.excel.parse.Texts.rowString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.Assert;

import com.jytec.cs.domain.Term;
import com.jytec.cs.excel.parse.HeaderRowNotFountException;
import com.jytec.cs.excel.parse.MergingAreas;
import com.jytec.cs.excel.parse.Regex;

public class TitleInfo {
	static Log log = LogFactory.getLog(TitleInfo.class);
	/** indexed by column index */
	protected Map<Integer, TimeInfo> timeInfos = new HashMap<>();
	// just for debug and help timeInfo when weekno, time-range in different rows.
	// Map<Integer, Integer> weeknos = new HashMap<>();
	/** for both theory and training schedule */
	protected int classColIndex = -1;
	/** for only training schedule */
	protected int weeknoColIndex = -1;
	protected Term parsedTerm;
	//
	protected int headerRowIndex, headerRowSpan;

	/** return the time-info {dayOfWeek, timeStart, timeEnd} for the corresponding cell */
	public TimeInfo getTimeInfo(Cell cell) {
		final TimeInfo timeInfo = timeInfos.get(cell.getColumnIndex());
		CellRangeAddress mergedArea = MergingAreas.getMergingArea(cell);
		if (mergedArea != null) {
			Assert.isTrue(mergedArea.getFirstColumn() == cell.getColumnIndex(), "Merging algorithm problems.");
			TimeInfo start = timeInfo, end = timeInfos.get(mergedArea.getLastColumn());
			if (start.dayOfWeek != end.dayOfWeek) {
				if(cell==null || cellString(cell).isEmpty()) {
					return start;
				}
				throw new IllegalArgumentException("未支持跨天的单元格合并！" + atLocaton(cell));
			}
			return new TimeInfo(start.dayOfWeek, start.timeStart, end.timeEnd);
		}
		return timeInfo;
	}

	public int getFollowingDataRowIndex() {
		return headerRowIndex + headerRowSpan;
	}

	/** dayOfWeek, timeStart, timeEnd */
	public static class TimeInfo {
		public final byte dayOfWeek, timeStart, timeEnd;

		public TimeInfo(byte dayOfWeek, byte timeStart, byte timeEnd) {
			this.dayOfWeek = dayOfWeek;
			this.timeStart = timeStart;
			this.timeEnd = timeEnd;
		}

		@Override
		public String toString() {
			return "星期" + dayOfWeekWords.charAt(dayOfWeek - 1) + "(" + timeStart + "," + timeEnd + ")";
		}
	}

	// ============ //
	// == Parser == //
	// ============ //

	static final String THORY_DAY_TIME_PATTERN = "([一二三四五六])/(\\d+)-(\\d+)";
	static final String weeknoColHeaderPattern = "周\\s*数";
	// 课表信息 for theory
	static final String classNameColHeaderPattern = "班\\s*级|课表信息";
	static final String dayOfWeekHeaderPattern = "星期([一二三四五六])";
	static final String dayOfWeekWords = "一二三四五六"; // for converting to integer.
	static final Pattern timeRangeSubHeaderPattern = Pattern.compile("(\\d+)[^\\d]+(\\d+)");
	// theory[序号]
	static final String otherAcceptableHeaderPattern = "系\\s*部|备\\s*注|序\\s*号";

	public static TitleInfo create(Sheet sheet, int headerRowIndex) {
		Assert.notNull(sheet, "Sheet 参数不应为空！");
		Row headerRow = sheet.getRow(headerRowIndex);
		if (headerRow == null) {
			throw new HeaderRowNotFountException(sheet);
		}
		TitleInfo titleInfo = new TitleInfo();
		titleInfo.headerRowIndex = headerRowIndex;
		for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
			Cell headerCell = headerRow.getCell(colIndex);
			String header = cellString(headerCell);
			if (header.isEmpty())
				continue;
			if (Pattern.matches(weeknoColHeaderPattern, header)) {
				titleInfo.headerRowSpan = MergingAreas.getCellRowSpan(headerCell); // row-span
				titleInfo.weeknoColIndex = headerCell.getColumnIndex();
			} else if (Pattern.matches(classNameColHeaderPattern, header)) {
				titleInfo.classColIndex = headerCell.getColumnIndex();
			} else if (Pattern.matches(THORY_DAY_TIME_PATTERN, header)) { // for theory-schedule
				Matcher m = Pattern.compile(THORY_DAY_TIME_PATTERN).matcher(header);
				if (!m.find()) {
					String msg = "未识别的表头【" + header + "】" + atLocaton(headerCell, false);
					throw new HeaderRowNotFountException(msg, sheet);
				}

				byte dayOfWeek = (byte) (dayOfWeekWords.indexOf(m.group(1)) + 1); // 0-based -> 1-baseFd
				byte timeStart = Byte.parseByte(m.group(2));
				byte timeEnd = Byte.parseByte(m.group(3));
				titleInfo.timeInfos.put(colIndex, new TimeInfo(dayOfWeek, timeStart, timeEnd));
			} else if (Pattern.matches(dayOfWeekHeaderPattern, header)) {
				String weekWord = Regex.group(1, Pattern.compile(dayOfWeekHeaderPattern), header);
				int dayOfWeek = dayOfWeekWords.indexOf(weekWord) + 1; // 0-based -> 1-baseFd

				Assert.isTrue(titleInfo.headerRowSpan > -1, "识别到列头星期，但暂未识别到周数列");
				int headerRowSpan = titleInfo.headerRowSpan;
				// handle sub headers for time-range[timeStart, timeEnd]
				CellRangeAddress ma = MergingAreas.getMergingArea(headerCell);
				Assert.isTrue(ma != null, "检测到星期表头异常，此处预期应该是合并的单元格" + atLocaton(headerCell));
				for (int colIndex4TR = ma.getFirstColumn(); colIndex4TR <= ma.getLastColumn(); colIndex4TR++) {
					for (int rowIndex = headerRowIndex + 1; rowIndex < headerRowIndex + headerRowSpan; rowIndex++) {
						Cell timeRangeCell = MergingAreas.getCell(sheet, rowIndex, colIndex4TR);
						String subHeader = cellString(timeRangeCell);
						if (!Regex.matchesPart(timeRangeSubHeaderPattern, subHeader))
							continue; // maybe a.m. | p.m. in 2nd row, time-ranges are in 3rd row in this case.

						String[] timeRange = Regex.groups(timeRangeSubHeaderPattern, subHeader);
						byte timeStart = Byte.parseByte(timeRange[1]), timeEnd = Byte.parseByte(timeRange[2]);
						TitleInfo.TimeInfo timeInfo = new TimeInfo((byte) dayOfWeek, timeStart, timeEnd);
						titleInfo.timeInfos.put(colIndex4TR, timeInfo);
						Assert.isTrue(colIndex4TR == timeRangeCell.getColumnIndex(), "课时列 index 计算错误！");
						log.debug("标记课时列【" + timeRangeCell + "】" + atLocaton(timeRangeCell) + timeInfo);
					}
				}
			} else if (!Pattern.matches(otherAcceptableHeaderPattern, header)) {
				String msg = "未识别的表头【" + header + "】" + atLocaton(headerCell, false);
				throw new HeaderRowNotFountException(msg, sheet);
			}
		}
		List<String> unfoundHeaders = new ArrayList<>();
		if (titleInfo.classColIndex == -1) {
			unfoundHeaders.add("周数");
		}
		if (titleInfo.timeInfos.isEmpty()) {
			unfoundHeaders.add("星期");
			unfoundHeaders.add("课时");
		}
		// TODO: validate weeknoColIndex for training.
		if (!unfoundHeaders.isEmpty()) {
			throw new HeaderRowNotFountException(headerRow, unfoundHeaders.toArray(new String[unfoundHeaders.size()]));
		}
		if(titleInfo.headerRowSpan<=0) {
			titleInfo.headerRowSpan = 1; // for theory-schedule.
		}
		return titleInfo;
	}

	public static TitleInfo search(Sheet sheet, int defaultRowIndex) {
		RuntimeException originalE;
		try {
			return create(sheet, defaultRowIndex);
		} catch (RuntimeException e) {
			originalE = e;
		}
		for (int rowIndex = 0; rowIndex <= 10 && rowIndex < sheet.getLastRowNum(); rowIndex++) {
			try {
				TitleInfo titleInfo = create(sheet, rowIndex);
				return titleInfo;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		throw originalE != null ? originalE : new HeaderRowNotFountException(sheet);
	}

	public static void searchAndValidateTerm(Sheet sheet, Term term) {
		Row titleRow = sheet.getRow(0);
		String title = rowString(titleRow);
		Term parsedTerm = TextParser.parseTerm(title);
		if (parsedTerm == null) {
			String msg = titleRow != null ? "无法从标题中识别学期：" + title + atLocaton(titleRow)
					: "无法从表格中识别学期（无标题行）" + atLocaton(sheet);
			log.warn(msg);
		} else if (parsedTerm.getTermYear() != term.getTermYear() || parsedTerm.getTermMonth() != term.getTermMonth()) {
			throw new IllegalArgumentException("表格标题【" + title + "】与指定的学期不同！" + atLocaton(titleRow));
		}
	}

}