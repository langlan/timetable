package com.jytec.cs.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jytec.cs.domain.Dept;
import com.jytec.cs.service.api.DeptSearchParams;

import langlan.sql.weaver.Sql;

@Service
public class DeptService extends ModelService<Dept> {

	public List<Dept> search(DeptSearchParams params) {
		Sql ql = new Sql().select("m").from("Dept m").where() //@formatter:off
				.grp(true)
					.like("m.name", params.q, true, true)
					.like("m.shortName", params.q, true, true)
					//.like("m.code", params.q, true, true)
				.endGrp()
				.eq("m.name", params.name)
				.eq("m.shortName", params.shortName)
				.eq("m.type", params.type)
			.endWhere(); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}

}
