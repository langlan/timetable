package com.jytec.cs.excel;

import static java.lang.String.join;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Site;
import com.jytec.cs.excel.parse.Columns;

@Service
public class SiteImporter {
	private static final Log log = LogFactory.getLog(SiteImporter.class);
	private @Autowired SiteRepository siteRepository;
	private @Autowired DeptRepository deptRepository;
	private Columns<Site> cols = new Columns<>(); // template.

	public SiteImporter() {
		cols.scol("教室编号", (it, m) -> m.setCode(TextParser.firstIntStr(it))); // some are number-type
		cols.scol("教室名称", (it, m) -> m.setName(it));
		cols.scol("座位数", (it, m) -> m.setCapacity(TextParser.parseInt(it))); // some are number-type
		cols.scol("教室类别", (it, m) -> m.setRoomType(it));
		cols.scol("最终调换", (it, m) -> m.setDept(new Dept(it)));
		cols.scol("多媒体改造", (it, m) -> m.setMultimedia(it));
		cols.scolOptional("校内实训基地名称", (it, m) -> m.setName4Training(it));
	}

	@Transactional
	public void importFile(File file) throws EncryptedDocumentException, IOException {
		Workbook wb = WorkbookFactory.create(file, null, true);
		doImport(wb);
		wb.close();
	}

	protected void doImport(Workbook wb) {
		// assertEquals(1, wb.getNumberOfSheets());
		int start = 0, end = 6;
		for (int i = start; i <= end; i++) {
			Sheet sheet = wb.getSheetAt(i);
			doImport(sheet);
		}
	}

	protected void doImport(Sheet sheet) {
		int headerRowIndex = 1, dataFirstRowIndex = 2;
		Row headerRow = sheet.getRow(headerRowIndex);
		BiConsumer<Row, Site> rowParser = cols.buildByHeaderRow(headerRow);
		if (rowParser == null) {
			log.warn("Ignore sheet :" + sheet.getSheetName());
			return;
		}

		// load keys for check;
		List<String[]> _keys = siteRepository.findAllLogicKeys(); // name + type
		Set<String> siteKeys = _keys.parallelStream().map(it -> join("", it)).collect(toSet());
		Map<String, Dept> depts = stream(deptRepository.findAll().spliterator(), false)
				.collect(toMap(it -> it.getName(), it -> it));

		int imported = 0, total = 0;
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
				total++;
				String key = site.getName() + site.getRoomType();
				if (siteKeys.add(key)) { // check existence.
					imported++;
					log.info("imported new site: " + key);
					siteRepository.save(site);
				} else {
					log.info("Ignore exist site: " + key);
				}
				if (i == dataFirstRowIndex) { // first row;
					mainDept = site.getDept();
				} else if (mainDept == null || !mainDept.getName().equals(deptName)) {
					mainDeptAllMatch = false;
				}
			}
		}
		//TODO: handle merging areas.
		log.info("Sheet[" + sheet.getSheetName() + "] 导入数据【" + imported + "/" + total + "】条");
		if (mainDept != null && mainDeptAllMatch && total > 10 && mainDept.getShortName() == null) {
			mainDept.setShortName(sheet.getSheetName());
		}

	}
}
