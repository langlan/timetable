package com.jytec.cs.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import javax.transaction.Transactional;

public class ModelService<T> extends CommonService {
	protected final Class<T> clazz;

	@SuppressWarnings("unchecked")
	public ModelService() {
		this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@Transactional
	public T get(Serializable id) {
		return dao.get(clazz, id);
	}
}
