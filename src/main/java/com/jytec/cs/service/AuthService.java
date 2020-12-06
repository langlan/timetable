package com.jytec.cs.service;

import static com.jytec.cs.domain.misc.Idc.IDC_MAX_VALUE;
import static com.jytec.cs.domain.misc.Idc.IDC_MIN_VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Teacher;

@Service
public class AuthService extends CommonService {

	@Transactional
	public void assignIdcs() {
		List<Teacher> teachers = dao.find("Select t From Teacher t Where t.idc is Null");
		List<Integer> idcs = generateIdcs(teachers.size(), Teacher.class);
		int index = 0;
		for (Teacher teacher : teachers) {
			teacher.setIdc(idcs.get(index++));
			dao.save(teacher);
//			Idc idc = new Idc();
//			idc.setId(teacher.getIdc());
//			idc.setUsed(Idc.USED_BY_TEACHER);
//			dao.save(idc);
		}
		Assert.isTrue(index == idcs.size(), "Wrong arithmetic.");

		List<Class> classes = dao.find("Select c From Class c Where c.idc is Null");
		idcs = generateIdcs(teachers.size(), Class.class);
		index = 0;
		for (Class theClass : classes) {
			theClass.setIdc(idcs.get(index++));
			dao.save(theClass);
//			Idc idc = new Idc();
//			idc.setId(theClass.getIdc());
//			idc.setUsed(Idc.USED_BY_CLASS);
//			dao.save(idc);
		}
		Assert.isTrue(index == idcs.size(), "Wrong arithmetic.");
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
}
