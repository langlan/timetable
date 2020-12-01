package com.jytec.cs.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.domain.Dept;

@SpringBootTest
public class ListenerTest {
	// private @Autowired Dao dao;
	private @Autowired DeptRepository deptRepository;
	
	@Test
	@Rollback
	public void testJapListener() {
		Dept d = new Dept();
		d.setName("TT");
		d = deptRepository.save(d);
		Date createdAt = d.getCreatedAt(),  updatedAt = d.getUpdatedAt();
		assertNotNull(createdAt);
		assertNotNull(updatedAt);
		
		d.setType("TT");
		d = deptRepository.save(d);
		assertEquals(createdAt, d.getCreatedAt());
		assertNotNull(d.getUpdatedAt());
		assertNotEquals(updatedAt, d.getUpdatedAt());
		
	}
}
