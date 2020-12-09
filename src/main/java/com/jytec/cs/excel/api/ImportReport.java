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

	public void append(SheetImportReport sheetImportPreview) {
		sheets.add(sheetImportPreview);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
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
		public int rowsTotal;
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

		public void log(String msg) {
			logs.add(msg);
		}

		@Override
		public String toString() {
			if (isIgnored()) {
				return sheetName + " - 已忽略此表： " + ignoredReason;
			}
			StringBuilder sb = new StringBuilder(sheetName).append(" - ");
			sb.append("数据总行数").append("【").append(rowsTotal).append("】");
			if (rowsIgnored > 0) {
				sb.append(" - ").append("已忽略行数").append(rowsIgnored).append("【").append("】");
			}
			sb.append("\r\n");
			if (!errorRows.isEmpty()) {
				sb.append("无效数据行").append("【").append(errorRows.size()).append("】");
				for (Integer key : errorRows.keySet()) {
					sb.append("  ");
					sb.append(key + 1);
					sb.append("：");
					sb.append(Strings.join(errorRows.get(key), ','));
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
