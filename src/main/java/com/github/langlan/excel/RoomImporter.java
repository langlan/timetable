package com.github.langlan.excel;

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

import com.github.langlan.dao.DeptRepository;
import com.github.langlan.dao.RoomRepository;
import com.github.langlan.domain.Dept;
import com.github.langlan.domain.Room;
import com.github.langlan.excel.parse.Columns;

@Service
public class RoomImporter {
	private static final Log log = LogFactory.getLog(RoomImporter.class);
	private @Autowired RoomRepository roomRepository;
	private @Autowired DeptRepository deptRepository;
	private Columns<Room> cols = new Columns<>(); // template.

	public RoomImporter() {
		cols.scol("教室编号", (it, room) -> room.setCode(TextParser.firstIntStr(it))); // some are number-type
		cols.scol("教室名称", (it, room) -> room.setName(it));
		cols.scol("座位数", (it, room) -> room.setCapcity(TextParser.parseInt(it))); // some are number-type
		cols.scol("教室类别", (it, room) -> room.setType(it));
		cols.scol("最终调换", (it, room) -> room.setDept(new Dept(it)));
		cols.scol("多媒体改造", (it, room) -> room.setMultimedia(it));
		cols.scolOptional("校内实训基地名称", (it, room) -> room.setName4Training(it));
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
		BiConsumer<Row, Room> rowParser = cols.buildByHeaderRow(headerRow);
		if (rowParser == null) {
			log.warn("Ignore sheet :" + sheet.getSheetName());
			return;
		}

		// load keys for check;
		// List<String> _keys = roomRepository.findAllLogicKeys();
		// Set<String> roomKeys = new HashSet<>(_keys);
		List<String[]> _keys = roomRepository.findAllLogicKeys(); // name + type
		Set<String> roomKeys = _keys.parallelStream().map(it -> join("", it)).collect(toSet());
		Map<String, Dept> depts = stream(deptRepository.findAll().spliterator(), false)
				.collect(toMap(it -> it.getName(), it -> it));

		int imported = 0, total = 0;
		Dept mainDept = null; // for save short name.
		boolean mainDeptAllMatch = true;

		// each dataRow
		for (int i = dataFirstRowIndex; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			Room room = new Room();
			rowParser.accept(row, room);
			String deptName = room.getDept().getName();
			room.setDept(null);

			if (!deptName.isEmpty()) { // re-set the department: find by name + type.
				room.setDept(depts.get(deptName));
				if (room.getDept() == null) {
					room.setDept(deptRepository.save(new Dept(deptName)));
					depts.put(deptName, room.getDept());
					deptRepository.save(room.getDept());
				}
			}

			if (!room.getName().isEmpty() && !room.getType().isEmpty()) { // not from empty row.
				total++;
				String key = room.getName() + room.getType();
				if (roomKeys.add(key)) { // check existence.
					imported++;
					log.info("imported new room: " + key);
					roomRepository.save(room);
				} else {
					log.info("Ignore exist room: " + key);
				}
				if (i == dataFirstRowIndex) { // first row;
					mainDept = room.getDept();
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
