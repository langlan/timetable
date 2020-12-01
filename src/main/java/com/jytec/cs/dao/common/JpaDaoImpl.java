package com.jytec.cs.dao.common;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JpaDaoImpl implements Dao {
	private @Autowired EntityManager em;

	@Override
	public <T> List<T> find(String ql, Object... vars) {
		Query q = prepareQuery(ql, vars);
		@SuppressWarnings("unchecked")
		List<T> list = q.getResultList();
		return list;
	}

	private Query prepareQuery(String ql, Object[] vars) {
		// JPA style ordinal parameters (e.g. ?1)
		String jpql = ql;
		if (vars.length > 0) {
			String[] _fs = ql.split("\\?");
			boolean funnyEnding = ql.endsWith("?");
			int count = funnyEnding ? _fs.length : _fs.length - 1;
			if (count != vars.length) {
				throw new IllegalArgumentException(
						"QL parameter placeholder counting error. expect[" + vars.length + "] but [" + count + "]");
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < vars.length; i++) {
				sb.append(_fs[i]);
				sb.append("?");
				String position = Integer.toString(i + 1);
				if (funnyEnding && i == vars.length - 1 || !_fs[i + 1].startsWith(position)) {
					sb.append(position);
				}
			}
			if (!ql.endsWith("?")) {
				sb.append(_fs[vars.length]);
			}
			jpql = sb.toString();

		}
		Query q = em.createQuery(jpql);
		// set parameters
		for (int i = 0; i < vars.length; i++) {
			q.setParameter(i + 1, vars[i]);
		}
		return q;
	}

	@Override
	public <T> T get(Class<T> clazz, Serializable id) {
		return em.find(clazz, id);
	}

}
