package com.jytec.cs.web;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jytec.cs.service.CommonService;

public class AbstractModelController<T, IDType extends Serializable> {
	private @Autowired CommonService service;
	protected final Class<T> clazz;

	@SuppressWarnings("unchecked")
	public AbstractModelController() {
		this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@GetMapping("/{id}")
	public T get(@PathVariable IDType id) {
		return service.get(clazz, id);
	}
}
