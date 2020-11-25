package com.github.langlan.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.Schedule;

@RepositoryRestResource(collectionResourceRel = "schedule", path = "schedule")
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	@Query("Select count(*) From Schedule s Where " //
			+ "s.termYear=?3 And s.termMonth=?4 And " //
			+ "s.theClass.name=?1 and s.theClass.degree=?2")
	int countByClassAndTerm(String className, String classDegree, short termYear, byte termMonth);
}
