package com.jytec.cs.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.jytec.cs.domain.Week;

@RepositoryRestResource(collectionResourceRel = "week", path = "week")
public interface WeekRepository extends JpaRepository<Week, String> {
	@Query("Select w From Week w Where w.termYear=?1 And w.termMonth=?2 and w.weekno=1")
	Optional<Week> findFirstWeek(short termYear, byte termMonth);
}
