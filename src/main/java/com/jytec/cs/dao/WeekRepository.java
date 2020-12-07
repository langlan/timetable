package com.jytec.cs.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jytec.cs.domain.Week;

//@RepositoryRestResource(collectionResourceRel = "week", path = "week")
public interface WeekRepository extends JpaRepository<Week, String> {
	
}
