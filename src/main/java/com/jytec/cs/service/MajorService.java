package com.jytec.cs.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.jytec.cs.domain.Major;
import com.jytec.cs.service.api.MajorSearchParams;

import langlan.sql.weaver.Sql;

@Service
public class MajorService extends ModelService<Major> {

	@Transactional
	public List<Major> search(MajorSearchParams params) {
		Sql ql = new Sql().select("m").from("Major m").where() //@formatter:off
			.grp(true)
				.like("m.name", params.q, true, true)
				.like("m.shortName", params.q, true, true)
				//.like("m.code", params.q, true, true)
			.endGrp()
			.eq("m.dept.id", params.deptId)
			.eq("m.degree", params.degree)
		.endWhere(); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}
}
