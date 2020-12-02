package com.jytec.cs.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import com.jytec.cs.dao.ClassRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Course;
import com.jytec.cs.service.api.ClassSearchParams;
import com.jytec.cs.service.api.CourseSearchParams;

import langlan.sql.weaver.Sql;

public class ClassCourseService extends CommonService{
	private @Autowired ClassRepository classRepository;

	@Transactional
	public List<com.jytec.cs.domain.Class> search(ClassSearchParams params) {
		Sql ql = new Sql().select("m").from("Class m").where() //@formatter:off
			.grp(true)
				.like("m.name", params.q, true, true)
				//.like("m.degree", params.q, true, true)
			.endGrp()
			.eq("m.major.id", params.majorId)
			.eq("m.degree", params.degree)
			.eq("m.name", params.name)
//			.eq("m.idc", params.idc)
		.endWhere()
		.orderBy("m.major.id, m.name"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}
	
	@Transactional
	public List<Course> search(CourseSearchParams params) {
		Sql ql = new Sql().select("m").from("Course m").where() //@formatter:off
			.grp(true)
				.like("m.name", params.q, true, true)
				.like("m.cate", params.q, true, true)
				.like("m.style", params.q, true, true)
				.like("m.method", params.q, true, true)
			.endGrp()
			// to be extending
		.endWhere()
		.orderBy("m.code"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	@Transactional
	public Optional<Class> findClassByIdc(Integer idc) {
		return classRepository.findByIdc(idc);
	}
	
	
}
