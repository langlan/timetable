package com.jytec.cs.service;

import java.io.Serializable;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.common.Dao;

@Primary
@Service
public class CommonService {
	protected @Autowired Dao dao;

	@Transactional
	public <M> M get(Class<M> clazz, Serializable id) {
		return dao.get(clazz, id);
	}
}
