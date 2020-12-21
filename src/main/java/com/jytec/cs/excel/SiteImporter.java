package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.firstInt;
import static com.jytec.cs.excel.parse.Texts.firstIntStr;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Site;
import com.jytec.cs.excel.api.ImportReport.SheetImportReport;
import com.jytec.cs.excel.parse.Columns;
import com.jytec.cs.excel.parse.HeaderRowNotFountException;
import com.jytec.cs.excel.parse.Texts;

@Service
public class SiteImporter extends AbstractImporter {
	private static final Log log = LogFactory.getLog(SiteImporter.class);
	private @Autowired SiteRepository siteRepository;
	private @Autowired DeptRepository deptRepository;
	private Columns<Site> cols = new Columns<>(); // template.

	public SiteImporter() {
		cols.scol("教室编号", (it, m) -> m.setCode(firstIntStr(it))); // some are number-type
		cols.scol("教室名称", (it, m) -> m.setName(it));
		cols.scol("座位数", (it, m) -> m.setCapacity(firstInt(it))); // some are number-type
		cols.scol("教室类别", (it, m) -> m.setRoomType(it));
		cols.scol("最终调换", (it, m) -> m.setDept(new Dept(it)));
		cols.scol("多媒体改造", (it, m) -> m.setMultimedia(it));
		cols.scol("校内实训基地名称", (it, m) -> m.setName4Training(it)).optional().withMerges();
	}
	
	@Override
	protected void doImport(Workbook wb, ImportContext context) {
		super.doImport(wb, context);
		if(context.params.preview) {
			context.reports.unsavedReason = "已指定导入预览参数。";
		}
	}

	@Override
	protected void doImport(Sheet sheet, ImportContext context) {
		SheetImportReport rpt = context.report;
		int headerRowIndex = 1, dataFirstRowIndex = 2;
		Row headerRow = sheet.getRow(headerRowIndex);
		BiConsumer<Row, Site> rowParser;
		try {
			rowParser = cols.buildRowProcessorByHeaderRow(headerRow);
		} catch (HeaderRowNotFountException e) {
			rpt.ignoredByReason("无法识别表头，疑似非数据表格 －" + e.getMessage());
			log.warn("Ignore sheet :" + sheet.getSheetName());
			return;
		}

		// load keys for check;
		Iterable<Site> allSites = siteRepository.findAll();
		// List<String[]> _keys = siteRepository.findAllLogicKeys(); // name + type
		// Set<String> siteKeys = _keys.parallelStream().map(it -> join("", it)).collect(toSet());
		Set<String> siteKeys = stream(allSites.spliterator(), false).map(it -> it.getName() + "-" + it.getRoomType())
				.collect(toSet());
		Map<String, Site> autoSitesIndexedByName = stream(allSites.spliterator(), false)
				.filter(it -> it.getCode() != null && it.getCode().startsWith("T"))
				.collect(toMap(Site::getName, it -> it));

		Map<String, Dept> depts = stream(deptRepository.findAll().spliterator(), false)
				.collect(toMap(it -> it.getName(), it -> it));

		Dept mainDept = null; // for save short name.
		boolean mainDeptAllMatch = true;

		// each dataRow
		for (int i = dataFirstRowIndex; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			Site site = new Site();
			rowParser.accept(row, site);
			String deptName = site.getDept().getName();
			site.setDept(null);

			if (!deptName.isEmpty()) { // re-set the department: find by name + type.
				site.setDept(depts.get(deptName));
				if (site.getDept() == null) {
					site.setDept(deptRepository.save(new Dept(deptName)));
					depts.put(deptName, site.getDept());
					deptRepository.save(site.getDept());
				}
			}

			if (!site.getName().isEmpty() && !site.getRoomType().isEmpty()) { // not from empty row.
				rpt.rowsTotal++;
				String key = site.getName() + "-" + site.getRoomType();
				if (siteKeys.add(key)) { // check existence.
					rpt.rowsReady++;
					String msg = Texts.isNotEmpty(site.getName4Training()) ? (key + " - " + site.getName4Training()) : key; 
					log.info(rpt.log("新增: " + msg));
					// use remove not get, to compatible with same-name records.
					Site exist = autoSitesIndexedByName.remove(site.getName());
					if (exist != null) {
						exist.setDept(site.getDept());
						exist.setCode(site.getCode());
						exist.setName4Training(site.getName4Training());
						exist.setRoomType(site.getRoomType());
						exist.setCapacity(site.getCapacity());
						exist.setMultimedia(site.getMultimedia());
						exist.setShortName(site.getShortName());
						exist.setMemo(site.getMemo());
						site = exist;
					}
					if(!context.params.preview) {
						siteRepository.save(site);	
					}
				} else {
					log.info(rpt.log("忽略已存在: " + key));
				}
				if (i == dataFirstRowIndex) { // first row;
					mainDept = site.getDept();
				} else if (mainDept == null || !mainDept.getName().equals(deptName)) {
					mainDeptAllMatch = false;
				}
			}
		}
		// TODO: handle merging areas.
		log.info("Sheet[" + sheet.getSheetName() + "] 导入数据【" + rpt.rowsReady + "/" + rpt.rowsTotal + "】条");
		if (mainDept != null && mainDeptAllMatch && rpt.rowsTotal > 10 && mainDept.getShortName() == null) {
			mainDept.setShortName(sheet.getSheetName());
		}

	}

}
