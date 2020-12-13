package com.jytec.cs.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jytec.cs.dao.common.Dao;
import com.jytec.cs.service.AuthService;

@SpringBootTest
public class AuthServiceTest {
	private @Autowired AuthService authService;
	private @Autowired Dao dao;
	
	@Test
	public void test() {
		authService.assignIdcs();
		assertEquals(0, dao.find("Select t From Teacher t Where t.idc is Null").size());
		assertEquals(0, dao.find("Select c From Class c Where c.idc is Null").size());
	}
	
	@Test
	public void testIdcsBackup() throws IOException {
		DateFormat format = new SimpleDateFormat("yyyMMdd");
		String fileName = "backup/idcs-" + format.format(new Date()) + ".xlsx";
		authService.backupIdcs(Files.of(fileName));
	}
	
	@Test
	public void testIdcsRestore() throws IOException {
		authService.restoreIdcs(Files.of("backup/idcs-20201213.xlsx"));
	}
	
	
}
