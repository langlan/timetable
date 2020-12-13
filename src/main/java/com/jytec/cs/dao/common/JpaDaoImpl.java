package com.jytec.cs.dao.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Component;

@Component
public class JpaDaoImpl implements Dao {
	private @Autowired EntityManager em;
	private Map<Class<?>, JpaEntityInformation<?, ?>> entityInfos = new HashMap<>();

	@Override
	public <T> List<T> find(String ql, Object... vars) {
		Query q = prepareQuery(ql, vars);
		@SuppressWarnings("unchecked")
		List<T> list = q.getResultList();
		return list;
	}

	@Override
	public List<Map<String, Object>> findMaps(String ql, Object... vars) {
		@SuppressWarnings("deprecation")
		Query q = prepareQuery(ql, vars).unwrap(org.hibernate.query.Query.class)
				.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = q.getResultList();
		return list;
	}
	
	@Override
	public Map<String, Object> findUniqueMap(String ql, Object... vars) {
		@SuppressWarnings("deprecation")
		Query q = prepareQuery(ql, vars).unwrap(org.hibernate.query.Query.class)
				.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		@SuppressWarnings("unchecked")
		Map<String, Object> ret = (Map<String, Object>) q.getSingleResult();
		return ret;
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

	@Override
	public <T> T save(T model) {
		if (isNew(model)) {
			em.persist(model);
			return model;
		} else {
			return em.merge(model);
		}
	}

	private <T> boolean isNew(T model) {
		if(model instanceof HibernateProxy) { // or test if managed-type by meta-model.
			return false;
		}
		return getEntityInfo(model).isNew(model);
	}

	// check-new only, for other uses, need do more work about proxy.
	<T> JpaEntityInformation<T, ?> getEntityInfo(T model) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) model.getClass();
		
		// Since we only use it for check-is-new. so no need to...
//		if(model instanceof HibernateProxy) {
//			@SuppressWarnings("unchecked")
//			Class<T> entityClass = ((HibernateProxy) model).getHibernateLazyInitializer().getPersistentClass();
//			clazz = entityClass;
//		}
		
		@SuppressWarnings("unchecked")
		JpaEntityInformation<T, ?> entityInfo = (JpaEntityInformation<T, ?>) entityInfos.get(clazz);
		if (entityInfo == null) {
			entityInfo = JpaEntityInformationSupport.getEntityInformation(clazz, em);
			entityInfos.put(clazz, entityInfo);
		}
		return entityInfo;

	}

	@Override
	public int update(String ql, Object... vars) {
		Query q = prepareQuery(ql, vars);
		return q.executeUpdate();
	}
	
	@Override
	public void flush() {
		em.flush();
	}
}
