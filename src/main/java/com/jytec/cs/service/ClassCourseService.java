package com.jytec.cs.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.ClassRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Course;
import com.jytec.cs.service.api.ClassSearchParams;
import com.jytec.cs.service.api.CourseSearchParams;

import langlan.sql.weaver.Sql;

@Service
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
			.eq("m.year", params.year)
		.endWhere()
		.orderBy("m.name"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}
	
	@Transactional
	public List<Course> search(CourseSearchParams params) {
		Sql ql = new Sql().select("m").from("Course m").where() //@formatter:off
			.grp(true)
				.like("m.name", params.q, true, true)
				.like("m.cate", params.q, true, true)
				.like("m.style", params.q, true, true)
				.like("m.examineMethod", params.q, true, true)
			.endGrp()
			.eq("m.code", params.code)
			.eq("m.name", params.name)
			.eq("m.cate", params.cate)
			.eq("m.style", params.style)
			.eq("m.examineMethod", params.examineMethod)
			.eq("m.labByTheory", params.labByTheory)
			.eq("m.locationType", params.locationType)
		.endWhere()
		.orderBy("m.code"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

	@Transactional
	public Optional<Class> findClassByIdc(Integer idc) {
		return classRepository.findByIdc(idc);
	}
	
	
}
