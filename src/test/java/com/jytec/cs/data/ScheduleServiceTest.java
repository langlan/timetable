package com.jytec.cs.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.jytec.cs.domain.schedule.ScheduleLesson;
import com.jytec.cs.service.ScheduleService;

@SpringBootTest
public class ScheduleServiceTest {
	private @Autowired ScheduleService scheduleService;
	private @Autowired EntityManager em;

	@Test
	@Transactional
	@Rollback(false)
	public void testInitScheduleLesson() {
		for (byte timeStart : new byte[] { 1, 3, 5, 7, 9 }) {
			for (byte timeEnd : new byte[] { 2, 4, 6, 8, 10 }) {
				if (timeStart < timeEnd) {
					ScheduleLesson.of((byte) timeStart, (byte) timeEnd).forEach(em::merge);
				}
			}
		}
	}

}
