package com.jytec.cs.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.jytec.cs.domain.Site;
import com.jytec.cs.service.api.SiteSearchParams;

import langlan.sql.weaver.Sql;

@Service
public class SiteService extends ModelService<Site> {

	@Transactional
	public List<Site> search(SiteSearchParams params) {
		Sql ql = new Sql().select("m").from("Site m").where() //@formatter:off
			.grp(true)
				.like("m.name", params.q, true, true)
				.like("m.name4Training", params.q, true, true)
				.like("m.roomType", params.q, true, true)
				.like("m.multimedia", params.q, true, true)
			.endGrp()
			//
		.endWhere()
		.orderBy("m.name"); //@formatter:on
		return dao.find(ql.toString(), ql.vars());
	}
}
