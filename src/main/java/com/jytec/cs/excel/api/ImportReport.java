package com.jytec.cs.excel.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;

/** output for confirm or discard */
public class ImportReport {
	List<SheetImportReport> sheets = new LinkedList<>();

	public void append(SheetImportReport sheetImportReport) {
		sheets.add(sheetImportReport);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("有效表单");
		sb.append("【");
		sb.append(sheets.stream().filter(it -> it.ignoredReason == null).count());
		sb.append("/");
		sb.append(sheets.size());
		sb.append("】");
		sb.append("个");
		sb.append(" - ");
		sb.append("共准备导入");
		sb.append("【");
		sb.append(sheets.stream().mapToInt(it -> it.rowsReady).sum());
		sb.append("/");
		sb.append(sheets.stream().mapToInt(it -> it.rowsTotal).sum());
		sb.append("】");
		sb.append("行");
		sb.append("\r\n");
		for (SheetImportReport sheet : sheets) {
			sb.append("===============\r\n");

			sb.append(sheet.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public static class SheetImportReport {
		private final String sheetName;
		private String ignoredReason;
		public int rowsTotal, rowsReady;
		public int rowsIgnored;
		private Map<Integer, Set<String>> errorRows = new LinkedHashMap<>();
		private List<String> logs = new LinkedList<>();

		public SheetImportReport(String sheetName) {
			this.sheetName = sheetName;
		}

		public boolean isIgnored() {
			return ignoredReason != null;
		}

		public void ignoredByReason(String message) {
			if (message == null)
				this.ignoredReason = "无法定位列头所在行，判定非数据表";
			ignoredReason = message;
		}

		public void logInvalidRow(int rowIndex, List<String> reasons) {
			Set<String> errors = errorRows.get(rowIndex);
			if (errors == null) {
				errors = new LinkedHashSet<>();
			}
			errors.addAll(reasons);
		}

		public void increseTotalRow() {
			rowsTotal++;
		}

		/** return the messaged passed-in as it is, can be used to log with log-framework */
		public String log(String msg) {
			logs.add(msg);
			return msg;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder().append("《").append(sheetName).append("》");
			if (isIgnored()) {
				sb.append("（已忽略）");
				sb.append(" - ");
				sb.append(ignoredReason);
			} else {
				sb.append(" - ");
				sb.append("准备导入").append("【").append(rowsReady).append("/").append(rowsTotal).append("】").append("行");
			}

			if (rowsIgnored > 0) {
				sb.append(" - ").append("已忽略行数").append(rowsIgnored).append("【").append("】");
			}
			sb.append("\r\n");
			if (!errorRows.isEmpty()) {
				sb.append("无效数据行").append("【").append(errorRows.size()).append("】");
				for (Integer rowNum : errorRows.keySet()) {
					sb.append("  ").append(rowNum + 1).append("：");
					sb.append(Strings.join(errorRows.get(rowNum), ','));
					sb.append("\r\n");
				}
			}
			if (!logs.isEmpty()) {
				for (String msg : logs) {
					sb.append(msg);
					sb.append("\r\n");
				}
			}
			return sb.toString();
		}
	}
}
