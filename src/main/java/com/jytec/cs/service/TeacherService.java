package com.jytec.cs.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.TeacherRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.service.api.TeacherSearchParams;

import langlan.sql.weaver.Sql;

@Service
public class TeacherService extends ModelService<Teacher>{
	private @Autowired TeacherRepository teacherRepository;

	@Transactional
	public List<Teacher> search(TeacherSearchParams params) {
		Sql ql = new Sql().select("m").from("Teacher m").where() //@formatter:off
			.grp(true)
				.like("m.name", params.q, true, true)
				.like("m.code", params.q, true, true)
				.like("m.phone", params.q, true, true)
				.like("m.mail", params.q, true, true)
				//.like("m.code", params.q, true, true)
			.endGrp()
			.eq("m.name", params.name)
			.eq("m.phone", params.phone)
			.eq("m.mail", params.mail)
			.eq("m.female", params.female)
		.endWhere(); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	public Optional<Class> findByIdc(Integer idc) {
		return teacherRepository.findByIdc(idc);
	}
}
