package com.jytec.cs.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jytec.cs.domain.misc.Idc;

public interface IdcRepository extends JpaRepository<Idc, Long> {
	// not use utype, since for teacher and class can not use a same name.

	@Query("Update Teacher m set m.idc=(Select i.id From Idc i Where i.name=m.name) "
			+ "Where m.idc Is Null And Exists(Select i.id From Idc i Where i.name=m.name)")
	int reassignTeacher();

	@Query("Update Class m set m.idc=(Select i.id From Idc i Where i.name=m.name) "
			+ "Where m.idc Is Null And Exists(Select i.id From Idc i Where i.name=m.name)")
	int reassignClass();

}
