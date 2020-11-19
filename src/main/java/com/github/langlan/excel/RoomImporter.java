package com.github.langlan.excel;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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

import com.github.langlan.dao.RoomRepository;
import com.github.langlan.domain.Room;
import com.github.langlan.excel.parse.Columns;

@Service
public class RoomImporter {
	private static final Log log = LogFactory.getLog(RoomImporter.class);
	private @Autowired RoomRepository roomRepository;
	private Columns<Room> cols = new Columns<>(); // template.
	
	public RoomImporter() {
		cols.scol("教室编号", (code, room) -> room.setCode(TextParser.firstIntStr(code))); // some are number-type
		cols.scol("教室名称", (code, room) -> room.setName(code));
		cols.scol("座位数", (code, room) -> room.setCapcity(TextParser.parseInt(code)));  // some are number-type
		cols.scol("教室类别", (code, room) -> room.setType(code));
		cols.scol("最终调换", (code, room) -> room.setUsage(code));
		cols.scol("多媒体改造", (code, room) -> room.setMultimedia(code));
		cols.scolOptional("校内实训基地名称", (code, room) -> room.setMultimedia(code));
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
		List<String[]> _keys = roomRepository.findAllLogicKeys();
		Set<String> roomKeys = new HashSet<>();
		_keys.forEach(it->roomKeys.add(String.join("", it)));

		int imported = 0;
		// each dataRow
		for (int i = dataFirstRowIndex; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			Room room = new Room();
			rowParser.accept(row, room);
			if (room.getName() != null && !room.getName().isEmpty()) {
				String key = room.getName() + room.getType();
				if (roomKeys.add(key)) { // check existence.
					imported++;
					log.info("imported new room: " + key);
					roomRepository.save(room);
				} else {
					log.warn("Ignore exist room: " + key);
				}

			}
		}
		log.info("Sheet[" + sheet.getSheetName() + "] 导入数据【" + imported + "/"
				+ (sheet.getLastRowNum() - dataFirstRowIndex + 1) + "】条");

	}
}
