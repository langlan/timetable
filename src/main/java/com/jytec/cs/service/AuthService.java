package com.jytec.cs.service;

import static com.jytec.cs.domain.misc.Idc.IDC_MAX_VALUE;
import static com.jytec.cs.domain.misc.Idc.IDC_MIN_VALUE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.IdcRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.misc.Idc;
import com.jytec.cs.excel.parse.Texts;

@Service
public class AuthService extends CommonService {
	private Log log = LogFactory.getLog(AuthService.class);
	private @Autowired IdcRepository idcRepository;

	@Transactional
	public void assignIdcs() {
		// try restore first
		assignFromBackup();

		List<Teacher> teachers = dao.find("Select t From Teacher t Where t.idc is Null");
		List<Integer> idcs = generateIdcs(teachers.size(), Teacher.class);
		int index = 0;
		for (Teacher teacher : teachers) {
			teacher.setIdc(idcs.get(index++));
			dao.save(teacher);
		}
		Assert.isTrue(index == idcs.size(), "Wrong arithmetic.");

		List<Class> classes = dao.find("Select c From Class c Where c.idc is Null");
		idcs = generateIdcs(classes.size(), Class.class);
		index = 0;
		for (Class theClass : classes) {
			theClass.setIdc(idcs.get(index++));
			dao.save(theClass);
		}
		Assert.isTrue(index == idcs.size(), "Wrong arithmetic.");

		log.info("Assign teacher-idc for new: " + teachers.size());
		log.info("Assign classes-idc for new: " + classes.size());
	}

	private void assignFromBackup() {
		log.info("Reassign teacher-idc from restored: " + idcRepository.reassignTeacher());
		log.info("Reassign classes-idc from restored: " + idcRepository.reassignClass());
		dao.flush();
	}

	private List<Integer> generateIdcs(int size, java.lang.Class<?> entityClass) {
		if (size <= 0) {
			return Collections.emptyList();
		}
		List<Integer> exist = dao
				.find("Select m.idc From " + entityClass.getSimpleName() + " m Where m.idc Is Not Null");
		Set<Integer> idcs = new HashSet<>(exist);
		List<Integer> ret = new ArrayList<>(size);
		Random random = new Random(System.currentTimeMillis());

		int bound = IDC_MAX_VALUE - IDC_MIN_VALUE;
		for (int i = 0; i < size; i++) {
			int r;
			do {
				r = random.nextInt(bound);
				r += IDC_MIN_VALUE;
			} while (!idcs.add(r));
			Assert.isTrue(IDC_MIN_VALUE <= r && r <= IDC_MAX_VALUE, "Wrong arithmetic.");
			ret.add(r);
		}
		return ret;
	}

	/** Restore from backup excel file to IDC table. */
	@Transactional
	public void restoreIdcs(File file) throws IOException {
		BiConsumer<Sheet, Byte> sheetConsumer = (sheet, type) -> {
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				Idc idc = new Idc();
				idc.setId(Integer.parseInt(Texts.cellString(row.getCell(0))));
				idc.setName(Texts.cellString(row.getCell(1)));
				idc.setMtype(type);
				idcRepository.save(idc);
			}
		};
		try (Workbook wb = WorkbookFactory.create(file, null, true)) {
			Sheet tsheet = wb.getSheetAt(0);
			Sheet csheet = wb.getSheetAt(1);
			sheetConsumer.accept(tsheet, Idc.USED_BY_TEACHER);
			sheetConsumer.accept(csheet, Idc.USED_BY_CLASS);
			idcRepository.flush();
		}
	}

	/**
	 * e.g. backupIdcs("/data/backup", "idcs-")
	 * @param dir      the backup dir.
	 * @param fileNamePrefix fileName without suffix(extension-name).
	 * @return created-file(dir+fileNamePrefix+yyyyMMddHHmmss.xlsx)
	 * @throws IOException
	 */
	@Transactional
	public File backupIdcs(File dir, String fileNamePrefix) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		List<Object[]> tIdcs = dao.find("Select m.name, m.idc From Teacher m Where m.idc Is Not Null");
		List<Object[]> tIdcs2 = dao.find("Select m.name, m.id From Idc m Where m.mtype=?", Idc.USED_BY_TEACHER);
		List<Object[]> cIdcs = dao.find("Select m.name, m.idc From Class m Where m.idc Is Not Null");
		List<Object[]> cIdcs2 = dao.find("Select m.name, m.id From Idc m Where m.mtype=?", Idc.USED_BY_CLASS);
		Map<String, Object> tmap = tIdcs.stream().collect(Collectors.toMap(it -> it[0].toString(), it -> it[1]));
		Map<String, Object> tmap2 = tIdcs2.stream().collect(Collectors.toMap(it -> it[0].toString(), it -> it[1]));
		Map<String, Object> cmap = cIdcs.stream().collect(Collectors.toMap(it -> it[0].toString(), it -> it[1]));
		Map<String, Object> cmap2 = cIdcs2.stream().collect(Collectors.toMap(it -> it[0].toString(), it -> it[1]));
		tmap.putAll(tmap2);
		cmap.putAll(cmap2);

		createSheet(workbook, tmap, "教师");
		createSheet(workbook, cmap, "班级");
		DateFormat format = new SimpleDateFormat("yyyMMddHHmmss");
		fileNamePrefix = fileNamePrefix + format.format(new Date()) + ".xlsx";
		File file = new File(dir, fileNamePrefix);
		file.getParentFile().mkdirs();

		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
		return file;
	}

	private final void createSheet(Workbook workbook, Map<String, Object> map, String sheetName) {
		Sheet sheet = workbook.createSheet(sheetName);
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Idc");
		headerRow.createCell(1).setCellValue("名称");
		int rowIndex = 1;
		for (Map.Entry<String, Object> e : map.entrySet()) {
			Row row = sheet.createRow(rowIndex++);
			row.createCell(0).setCellValue(e.getValue().toString());
			row.createCell(1).setCellValue(e.getKey());
		}
	}
}
